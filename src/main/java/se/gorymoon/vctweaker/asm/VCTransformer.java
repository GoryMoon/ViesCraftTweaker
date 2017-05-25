package se.gorymoon.vctweaker.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;
import se.gorymoon.vctweaker.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * The base setup of this class is by hilburn
 * It's a very nice way of setting up the transformer
 */
public class VCTransformer implements IClassTransformer, Opcodes {

    private enum TransformType
    {
        METHOD, FIELD, INNER_CLASS, MODIFY, MAKE_PUBLIC, DELETE
    }

    private enum Transformer
    {
        REPLACE_JEI("register", "(Lmezz/jei/api/IModRegistry;)V") {

            @Override
            protected InsnList modifyInstructions(InsnList list) {
                LabelNode label1 = new LabelNode();
                LabelNode label2 = new LabelNode();

                AbstractInsnNode node = list.getFirst();
                while (node.getOpcode() != ALOAD) node = node.getNext();
                list.insertBefore(node, new FieldInsnNode(GETSTATIC, "se/gorymoon/vctweaker/VCTweaker$Configs", "replaceJEI", "Z"));
                list.insertBefore(node, new JumpInsnNode(IFEQ, label1));
                list.insertBefore(node, new LabelNode());
                list.insertBefore(node, new VarInsnNode(ALOAD, 1));
                list.insertBefore(node, new MethodInsnNode(INVOKESTATIC, "se/gorymoon/vctweaker/jei/JEIPlugin", "register", "(Lmezz/jei/api/IModRegistry;)V", false));
                list.insertBefore(node, new JumpInsnNode(GOTO, label2));
                list.insertBefore(node, label1);

                list.insertBefore(node, new FrameNode(F_SAME, 0, new Object[]{}, 0 , new Object[]{}));
                while (node.getOpcode() != AASTORE) node = node.getNext();
                while (node.getOpcode() != ALOAD) node = node.getNext();
                list.insertBefore(node, label2);
                list.insertBefore(node, new FrameNode(F_SAME, 0, new Object[]{}, 0 , new Object[]{}));

                return list;
            }
        },
        REMOVE_WORKBENCH_R_CLOSING("func_73869_a", "(CI)V") {
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
        REPLACE_FUELHANDLING("getItemBurnTime", "(Lnet/minecraft/item/ItemStack;)I") {
            @Override
            protected InsnList modifyInstructions(InsnList list) {
                list.clear();
                list.add(new LabelNode());
                list.add(new VarInsnNode(ALOAD, 0));
                list.add(new MethodInsnNode(INVOKESTATIC, "se/gorymoon/vctweaker/fuel/FuelHandler", "getItemBurnTime", "(Lnet/minecraft/item/ItemStack;)I", false));
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
        PRINT("getItemBurnTime", "(Lnet/minecraft/item/ItemStack;)I") {
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
                boolean printCompact = false;
                for (int i = 0; i < list.size(); i++) {
                    if (printCompact) {
                        System.out.println(insnToString(list.get(i)));
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

        Transformer(String name)
        {
            this(name, "", TransformType.INNER_CLASS, TransformType.MAKE_PUBLIC);
        }

        Transformer(String name, String args)
        {
            this(name, args, TransformType.METHOD, TransformType.MODIFY);
        }

        Transformer(String name, String args, TransformType type, TransformType action)
        {
            this.name = name;
            this.args = args;
            this.type = type;
            this.action = action;
        }

        protected InsnList modifyInstructions(InsnList list)
        {
            return list;
        }

        private static InsnList replace(InsnList list, String toReplace, String replace)
        {
            AbstractInsnNode node = list.getFirst();
            InsnList result = new InsnList();
            while (node != null)
            {
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

        protected void methodTransform(ClassNode node)
        {
            MethodNode methodNode = getMethod(node);
            if (methodNode != null)
            {
                switch (action)
                {
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

        private void fieldTransform(ClassNode node)
        {
            FieldNode fieldNode = getField(node);
            if (fieldNode != null)
            {
                switch (action)
                {
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

        private void modifyField(FieldNode fieldNode)
        {
        }


        private void innerClassTransform(ClassNode node)
        {
            InnerClassNode innerClassNode = getInnerClass(node);
            if (innerClassNode != null)
            {
                switch (action)
                {
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

        private void modifyInnerClass(InnerClassNode innerClassNode)
        {
        }

        public void transform(ClassNode node)
        {
            switch (this.type)
            {
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

        private static AbstractInsnNode checkReplace(AbstractInsnNode node, String toReplace, String replace)
        {
            if (node instanceof TypeInsnNode && ((TypeInsnNode)node).desc.equals(toReplace))
            {
                return new TypeInsnNode(NEW, replace);
            } else if (node instanceof MethodInsnNode && ((MethodInsnNode)node).owner.contains(toReplace))
            {
                return new MethodInsnNode(node.getOpcode(), replace, ((MethodInsnNode)node).name, ((MethodInsnNode)node).desc, false);
            }
            return node;
        }

        public void complete()
        {
            Log.info("Applied " + this + " transformer");
        }

        public MethodNode getMethod(ClassNode classNode)
        {
            for (MethodNode method : classNode.methods)
            {
                if (method.name.equals(getName()) && method.desc.equals(getArgs()))
                {
                    return method;
                }
            }
            for (MethodNode method : classNode.methods)
            {
                if (method.desc.equals(getArgs()))
                {
                    return method;
                }
            }
            return null;
        }

        public FieldNode getField(ClassNode classNode)
        {
            for (FieldNode field : classNode.fields)
            {
                if (field.name.equals(getName()) && field.desc.equals(getArgs()))
                {
                    return field;
                }
            }
            return null;
        }

        public InnerClassNode getInnerClass(ClassNode classNode)
        {
            String name = classNode.name + "$" + getName();
            for (InnerClassNode inner : classNode.innerClasses)
            {
                if (name.equals(inner.name))
                {
                    return inner;
                }
            }
            return null;
        }
    }

    private enum ClassName
    {
        JEI_PLUGIN("com.viesis.viescraft.api.jei.JEIPlugin", Transformer.REPLACE_JEI),
        GUI_WORKBENCH("com.viesis.viescraft.client.gui.GuiTileEntityAirshipWorkbench", Transformer.REMOVE_WORKBENCH_R_CLOSING),
        AIRSHIP_V1("com.viesis.viescraft.common.entity.airshipcolors.EntityAirshipV1Core", Transformer.REPLACE_FUELHANDLING),
        AIRSHIP_V2("com.viesis.viescraft.common.entity.airshipcolors.EntityAirshipV2Core", Transformer.REPLACE_FUELHANDLING),
        AIRSHIP_V3("com.viesis.viescraft.common.entity.airshipcolors.EntityAirshipV3Core", Transformer.REPLACE_FUELHANDLING),
        AIRSHIP_V4("com.viesis.viescraft.common.entity.airshipcolors.EntityAirshipV4Core", Transformer.REPLACE_FUELHANDLING);

        private String name;
        private Transformer[] transformers;

        ClassName(String name, Transformer... transformers)
        {
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

        public byte[] transform(byte[] bytes)
        {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(bytes);
            classReader.accept(classNode, 0);

            Log.info("Applying Transformer" + (transformers.length > 1 ? "s " : " ") + "to " + getName());

            for (Transformer transformer : getTransformers())
            {
                transformer.transform(classNode);
            }

            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            classNode.accept(writer);
            return writer.toByteArray();
        }
    }

    private static Map<String, ClassName> classMap = new HashMap<String, ClassName>();

    static
    {
        for (ClassName className : ClassName.values()) classMap.put(className.getName(), className);
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        ClassName clazz = classMap.get(name);
        if (clazz != null)
        {
            basicClass = clazz.transform(basicClass);
            classMap.remove(name);
        }
        return basicClass;
    }


}
