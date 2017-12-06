package se.gory_moon.vctweaker.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.classloading.FMLForgePlugin;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;
import se.gory_moon.vctweaker.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * The base setup of this class is by hilburn
 * It's a very nice way of setting up the transformer
 */
public class VCTransformer implements IClassTransformer, Opcodes {

    private enum TransformType {
        METHOD, FIELD, INNER_CLASS, MODIFY, MAKE_PUBLIC, DELETE, ADD
    }

    private enum Transformer {
        REPLACE_JEI("register", "(Lmezz/jei/api/IModRegistry;)V") {

            @Override
            protected InsnList modifyInstructions(InsnList list) {
                AbstractInsnNode node = list.getFirst();
                while (node.getOpcode() != ALOAD) node = node.getNext();

                LabelNode label1 = new LabelNode();
                list.insertBefore(node, new FieldInsnNode(GETSTATIC, "se/gory_moon/vctweaker/VCTweakerContainer$Configs", "replaceJEI", "Z"));
                list.insertBefore(node, new JumpInsnNode(IFNE, label1));
                list.insertBefore(node, new LabelNode());

                while (node.getOpcode() != AASTORE) node = node.getNext();
                while (node.getOpcode() != ALOAD) node = node.getNext();
                list.insertBefore(node, label1);
                list.insertBefore(node, new FrameNode(F_SAME, 0, new Object[]{}, 0 , new Object[]{}));

                while (true) {
                    if (node.getOpcode() == ALOAD && ((VarInsnNode) node).var == 3)
                        break;
                    node = node.getNext();
                }

                LabelNode label2 = new LabelNode();
                list.insertBefore(node, new FieldInsnNode(GETSTATIC, "se/gory_moon/vctweaker/VCTweakerContainer$Configs", "replaceJEI", "Z"));
                list.insertBefore(node, new JumpInsnNode(IFNE, label2));
                list.insertBefore(node, new LabelNode());

                while (node.getOpcode() != RETURN) node = node.getNext();
                list.insertBefore(node, label2);
                list.insertBefore(node, new FrameNode(F_SAME, 0, new Object[]{}, 0, new Object[]{}));

                return list;
            }
        },
        REMOVE_WORKBENCH_R_CLOSING(FMLForgePlugin.RUNTIME_DEOBF? "func_73869_a": "keyTyped", "(CI)V") {
            @Override
            protected InsnList modifyInstructions(InsnList list) {
                AbstractInsnNode node = list.getFirst();
                AbstractInsnNode base = list.getFirst();

                while(node.getOpcode() != IF_ICMPEQ) {
                    node = node.getNext();
                    base = base.getNext();
                }
                node = node.getNext();

                while(node.getOpcode() != ALOAD) {
                    list.remove(node);
                    node = base.getNext();
                }

                return list;
            }
        },
        FIX_NO_GUI_TOOLTIP(FMLForgePlugin.RUNTIME_DEOBF ? "func_73863_a": "drawScreen", "(IIF)V", TransformType.METHOD, TransformType.ADD) {
            @Override
            protected InsnList modifyInstructions(InsnList list) {
                list.add(new LabelNode());
                list.add(new VarInsnNode(ALOAD, 0));
                list.add(new MethodInsnNode(INVOKESTATIC, "se/gory_moon/vctweaker/asm/VCHooks", "drawDefaultBack", "(Lnet/minecraft/client/gui/inventory/GuiContainer;)V", false));
                list.add(new LabelNode());
                list.add(new VarInsnNode(ALOAD, 0));
                list.add(new VarInsnNode(ILOAD, 1));
                list.add(new VarInsnNode(ILOAD, 2));
                list.add(new VarInsnNode(FLOAD, 3));
                list.add(new MethodInsnNode(INVOKESPECIAL, "net/minecraft/client/gui/inventory/GuiContainer", FMLForgePlugin.RUNTIME_DEOBF ? "func_73863_a": "drawScreen", "(IIF)V", false));
                list.add(new LabelNode());
                list.add(new VarInsnNode(ALOAD, 0));
                list.add(new VarInsnNode(ILOAD, 1));
                list.add(new VarInsnNode(ILOAD, 2));
                list.add(new MethodInsnNode(INVOKESPECIAL, "net/minecraft/client/gui/inventory/GuiContainer", FMLForgePlugin.RUNTIME_DEOBF ? "func_191948_b": "renderHoveredToolTip", "(II)V", false));
                list.add(new LabelNode());
                list.add(new InsnNode(RETURN));
                list.add(new LabelNode());
                return list;
            }
        },
        REPLACE_FUELHANDLING("getItemBurnTime", "(Lnet/minecraft/item/ItemStack;)I") {
            @Override
            protected InsnList modifyInstructions(InsnList list) {
                list.clear();
                list.add(new LabelNode());
                list.add(new VarInsnNode(ALOAD, 0));
                list.add(new MethodInsnNode(INVOKESTATIC, "se/gory_moon/vctweaker/fuel/FuelHandler", "getItemBurnTime", "(Lnet/minecraft/item/ItemStack;)I", false));
                list.add(new InsnNode(IRETURN));
                list.add(new LabelNode());
                return list;
            }
        },
        /**
         * Helper for printing the structure of a method
         * First parameter is the method name and second is the description of it
         * Change the boolean printCompact for either all code easily copy and pastable to somewhere else
         * or separated with the name of the class to use for the specific call
         */
        PRINT("drawScreen", "(IIF)V") {
            private Printer printer;
            private TraceMethodVisitor mp;

            public String insnToString(AbstractInsnNode insn){
                insn.accept(mp);
                StringWriter sw = new StringWriter();
                printer.print(new PrintWriter(sw));
                printer.getText().clear();
                return sw.toString();
            }

            @Override
            protected InsnList modifyInstructions(InsnList list) {
                printer = new Textifier();
                mp = new TraceMethodVisitor(printer);
                boolean printCompact = true;
                for (int i = 0; i < list.size(); i++) {
                    if (printCompact) {
                        System.out.print(insnToString(list.get(i)));
                    } else {
                        Log.info(String.valueOf(list.get(i)));
                        Log.info(insnToString(list.get(i)));
                    }
                }
                return super.modifyInstructions(list);
            }
        };

        protected String name;
        protected String args;
        protected TransformType type;
        protected TransformType action;

        Transformer(String name) {
            this(name, "", TransformType.INNER_CLASS, TransformType.MAKE_PUBLIC);
        }

        Transformer(String name, String args) {
            this(name, args, TransformType.METHOD, TransformType.MODIFY);
        }

        Transformer(String name, String args, TransformType type, TransformType action) {
            this.name = name;
            this.args = args;
            this.type = type;
            this.action = action;
        }

        protected InsnList modifyInstructions(InsnList list)
        {
            return list;
        }

        private static InsnList replace(InsnList list, String toReplace, String replace) {
            AbstractInsnNode node = list.getFirst();
            InsnList result = new InsnList();
            while (node != null) {
                result.add(checkReplace(node, toReplace, replace));
                node = node.getNext();
            }
            return result;
        }

        public String getName()
        {
            return name;
        }

        public String getArgs()
        {
            return args;
        }

        protected void methodTransform(ClassNode node) {
            MethodNode methodNode = getMethod(node);
            if (methodNode == null && action == TransformType.ADD) {
                methodNode = new MethodNode(ASM4, ACC_PUBLIC, name, args, null, null);
                node.methods.add(methodNode);
            }

            if (methodNode != null) {
                switch (action) {
                    case ADD:
                    case MODIFY:
                        methodNode.instructions = modifyInstructions(methodNode.instructions);
                        break;
                    case DELETE:
                        node.methods.remove(methodNode);
                        break;
                    case MAKE_PUBLIC:
                        methodNode.access = (methodNode.access & ~7) ^ 1;
                }
                complete();
            }
        }

        private void fieldTransform(ClassNode node) {
            FieldNode fieldNode = getField(node);
            if (fieldNode != null) {
                switch (action) {
                    case MODIFY:
                        modifyField(fieldNode);
                        break;
                    case DELETE:
                        node.fields.remove(fieldNode);
                        break;
                    case MAKE_PUBLIC:
                        fieldNode.access = (fieldNode.access & ~7) ^ 1;
                }
                complete();
            }
        }

        private void modifyField(FieldNode fieldNode) {
        }


        private void innerClassTransform(ClassNode node) {
            InnerClassNode innerClassNode = getInnerClass(node);
            if (innerClassNode != null) {
                switch (action) {
                    case MODIFY:
                        modifyInnerClass(innerClassNode);
                        break;
                    case DELETE:
                        node.innerClasses.remove(innerClassNode);
                        break;
                    case MAKE_PUBLIC:
                        innerClassNode.access = (innerClassNode.access & ~7) ^ 1;
                }
                complete();
            }
        }

        private void modifyInnerClass(InnerClassNode innerClassNode) {
        }

        public void transform(ClassNode node) {
            switch (this.type) {
                case METHOD:
                    methodTransform(node);
                    return;
                case FIELD:
                    fieldTransform(node);
                    return;
                case INNER_CLASS:
                    innerClassTransform(node);
            }
        }

        private static AbstractInsnNode checkReplace(AbstractInsnNode node, String toReplace, String replace) {
            if (node instanceof TypeInsnNode && ((TypeInsnNode)node).desc.equals(toReplace)) {
                return new TypeInsnNode(NEW, replace);
            } else if (node instanceof MethodInsnNode && ((MethodInsnNode)node).owner.contains(toReplace)) {
                return new MethodInsnNode(node.getOpcode(), replace, ((MethodInsnNode)node).name, ((MethodInsnNode)node).desc, false);
            }
            return node;
        }

        public void complete()
        {
            Log.info("Applied " + this + " transformer");
        }

        public MethodNode getMethod(ClassNode classNode) {
            for (MethodNode method : classNode.methods) {
                if (method.name.equals(getName()) && method.desc.equals(getArgs())) {
                    return method;
                }
            }
            for (MethodNode method : classNode.methods) {
                if (method.desc.equals(getArgs())) {
                    return method;
                }
            }
            return null;
        }

        public FieldNode getField(ClassNode classNode) {
            for (FieldNode field : classNode.fields) {
                if (field.name.equals(getName()) && field.desc.equals(getArgs())) {
                    return field;
                }
            }
            return null;
        }

        public InnerClassNode getInnerClass(ClassNode classNode) {
            String name = classNode.name + "$" + getName();
            for (InnerClassNode inner : classNode.innerClasses) {
                if (name.equals(inner.name)) {
                    return inner;
                }
            }
            return null;
        }
    }

    private enum ClassName {
        //TEST("se.gory_moon.vctweaker.TestGuiContainer", Transformer.PRINT),
        GUI_WORKBENCH("com.viesis.viescraft.client.gui.GuiTileEntityAirshipWorkbench", Transformer.FIX_NO_GUI_TOOLTIP, Transformer.REMOVE_WORKBENCH_R_CLOSING),

        GUI_AIRSHIP_MENU("com.viesis.viescraft.client.gui.airship.main.GuiAirshipMenu", Transformer.FIX_NO_GUI_TOOLTIP),
        GUI_AIRSHIP_UPGRADE("com.viesis.viescraft.client.gui.airship.main.GuiUpgradeMenu", Transformer.FIX_NO_GUI_TOOLTIP),
        GUI_AIRSHIP_VISUAL("com.viesis.viescraft.client.gui.airship.main.GuiVisualMenu", Transformer.FIX_NO_GUI_TOOLTIP),
        GUI_AIRSHIP_MODULE("com.viesis.viescraft.client.gui.airship.main.GuiModuleMenu", Transformer.FIX_NO_GUI_TOOLTIP),
        GUI_AIRSHIP_MODULE_INV("com.viesis.viescraft.client.gui.airship.main.GuiAirshipMenuStorageNormal", Transformer.FIX_NO_GUI_TOOLTIP),
        GUI_AIRSHIP_MODULE_INV_SMALL("com.viesis.viescraft.client.gui.airship.main.GuiAirshipMenuStorageLesser", Transformer.FIX_NO_GUI_TOOLTIP),
        GUI_AIRSHIP_MODULE_INV_LARGE("com.viesis.viescraft.client.gui.airship.main.GuiAirshipMenuStorageGreater", Transformer.FIX_NO_GUI_TOOLTIP),
        GUI_AIRSHIP_MUSIC("com.viesis.viescraft.client.gui.airship.main.GuiAirshipMenuMusic", Transformer.FIX_NO_GUI_TOOLTIP),
        GUI_AIRSHIP_MUSIC_1("com.viesis.viescraft.client.gui.airship.music.GuiAirshipMusicPg1", Transformer.FIX_NO_GUI_TOOLTIP),
        GUI_AIRSHIP_BALLOON("com.viesis.viescraft.client.gui.airship.visual.balloon.GuiVisualMenuBalloon", Transformer.FIX_NO_GUI_TOOLTIP),
        GUI_AIRSHIP_BALLOON_COLOR("com.viesis.viescraft.client.gui.airship.visual.balloon.GuiVisualMenuBalloonColor", Transformer.FIX_NO_GUI_TOOLTIP),
        GUI_AIRSHIP_BALLOON_T1("com.viesis.viescraft.client.gui.airship.visual.balloon.GuiVisualMenuBalloonTier1Pg1", Transformer.FIX_NO_GUI_TOOLTIP),
        GUI_AIRSHIP_BALLOON_T2("com.viesis.viescraft.client.gui.airship.visual.balloon.GuiVisualMenuBalloonTier2Pg1", Transformer.FIX_NO_GUI_TOOLTIP),
        GUI_AIRSHIP_BALLOON_T3("com.viesis.viescraft.client.gui.airship.visual.balloon.GuiVisualMenuBalloonTier3Pg1", Transformer.FIX_NO_GUI_TOOLTIP),
        GUI_AIRSHIP_BALLOON_T4("com.viesis.viescraft.client.gui.airship.visual.balloon.GuiVisualMenuBalloonTier4Pg1", Transformer.FIX_NO_GUI_TOOLTIP),
        GUI_AIRSHIP_BALLOON_T5("com.viesis.viescraft.client.gui.airship.visual.balloon.GuiVisualMenuBalloonTier5Pg1", Transformer.FIX_NO_GUI_TOOLTIP),
        GUI_AIRSHIP_FRAME("com.viesis.viescraft.client.gui.airship.visual.frame.GuiVisualMenuFrame", Transformer.FIX_NO_GUI_TOOLTIP),
        GUI_AIRSHIP_FRAME_COLOR("com.viesis.viescraft.client.gui.airship.visual.frame.GuiVisualMenuFrameColor", Transformer.FIX_NO_GUI_TOOLTIP),
        GUI_AIRSHIP_FRAME_T1("com.viesis.viescraft.client.gui.airship.visual.frame.GuiVisualMenuFrameTier1Pg1", Transformer.FIX_NO_GUI_TOOLTIP),
        GUI_AIRSHIP_FRAME_T1P2("com.viesis.viescraft.client.gui.airship.visual.frame.GuiVisualMenuFrameTier1Pg2", Transformer.FIX_NO_GUI_TOOLTIP),
        GUI_AIRSHIP_FRAME_T2("com.viesis.viescraft.client.gui.airship.visual.frame.GuiVisualMenuFrameTier2Pg1", Transformer.FIX_NO_GUI_TOOLTIP),
        GUI_AIRSHIP_FRAME_T3("com.viesis.viescraft.client.gui.airship.visual.frame.GuiVisualMenuFrameTier3Pg1", Transformer.FIX_NO_GUI_TOOLTIP),
        GUI_AIRSHIP_FRAME_T4("com.viesis.viescraft.client.gui.airship.visual.frame.GuiVisualMenuFrameTier4Pg1", Transformer.FIX_NO_GUI_TOOLTIP),
        GUI_AIRSHIP_FRAME_T5("com.viesis.viescraft.client.gui.airship.visual.frame.GuiVisualMenuFrameTier51Pg1", Transformer.FIX_NO_GUI_TOOLTIP),
        AIRSHIP_V1("com.viesis.viescraft.common.entity.airshipcolors.EntityAirshipV1Core", Transformer.REPLACE_FUELHANDLING),
        AIRSHIP_V2("com.viesis.viescraft.common.entity.airshipcolors.EntityAirshipV2Core", Transformer.REPLACE_FUELHANDLING),
        AIRSHIP_V3("com.viesis.viescraft.common.entity.airshipcolors.EntityAirshipV3Core", Transformer.REPLACE_FUELHANDLING),
        AIRSHIP_V4("com.viesis.viescraft.common.entity.airshipcolors.EntityAirshipV4Core", Transformer.REPLACE_FUELHANDLING),
        AIRSHIP_V5("com.viesis.viescraft.common.entity.airshipcolors.EntityAirshipV5Core", Transformer.REPLACE_FUELHANDLING),
        AIRSHIP_V6("com.viesis.viescraft.common.entity.airshipcolors.EntityAirshipV6Core", Transformer.REPLACE_FUELHANDLING);

        private String name;
        private Transformer[] transformers;

        ClassName(String name, Transformer... transformers) {
            this.name = name;
            this.transformers = transformers;
        }

        public String getName()
        {
            return name;
        }

        public Transformer[] getTransformers()
        {
            return transformers;
        }

        public byte[] transform(byte[] bytes) {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(bytes);
            classReader.accept(classNode, 0);

            Log.info("Applying Transformer" + (transformers.length > 1 ? "s " : " ") + "to " + getName());

            for (Transformer transformer : getTransformers()) {
                transformer.transform(classNode);
            }

            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            classNode.accept(writer);
            return writer.toByteArray();
        }
    }

    private static Map<String, ClassName> classMap = new HashMap<String, ClassName>();

    static {
        for (ClassName className : ClassName.values()) classMap.put(className.getName(), className);
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        ClassName clazz = classMap.get(name);
        if (clazz != null) {
            basicClass = clazz.transform(basicClass);
            classMap.remove(name);
        }
        return basicClass;
    }


}
