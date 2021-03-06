/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000-2011 INRIA, France Telecom
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package saferefactor.core.analysis.safira.visitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import org.apache.tools.ant.taskdefs.Replace;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import saferefactor.core.analysis.safira.analyzer.Tools;

import saferefactor.core.analysis.safira.entity.Field;
import saferefactor.core.analysis.safira.entity.InstructionNode;
import saferefactor.core.analysis.safira.entity.Method;

public class MethodNode extends MethodVisitor {

    /**
     * The method's access flags (see {@link Opcodes}). This field also
     * indicates if the method is synthetic and/or deprecated.
     */
    public int access;
    
    /**
     * The method's name.
     */
    public String name;

    /**
     * The method's descriptor (see {@link Type}).
     */
    public String desc;

    /**
     * The method's signature. May be <tt>null</tt>.
     */
    public String signature;

    /**
     * The internal names of the method's exception classes (see
     * {@link Type#getInternalName() getInternalName}). This list is a list of
     * {@link String} objects.
     */
    public List<String> exceptions;

    /**
     * The runtime visible annotations of this method. This list is a list of
     * {@link AnnotationNode} objects. May be <tt>null</tt>.
     *
     * @associates org.objectweb.asm.tree.AnnotationNode
     * @label visible
     */
    public List<AnnotationNode> visibleAnnotations;
    
    public List<Field> fieldInvocations = new ArrayList<Field>();

    /**
     * The runtime invisible annotations of this method. This list is a list of
     * {@link AnnotationNode} objects. May be <tt>null</tt>.
     *
     * @associates org.objectweb.asm.tree.AnnotationNode
     * @label invisible
     */
    public List<AnnotationNode> invisibleAnnotations;

    /**
     * The non standard attributes of this method. This list is a list of
     * {@link Attribute} objects. May be <tt>null</tt>.
     *
     * @associates org.objectweb.asm.Attribute
     */
    public List<Attribute> attrs;
    
    public List<Method> methodInvocations = new ArrayList<Method>();
    /**
     * The default value of this annotation interface method. This field must be
     * a {@link Byte}, {@link Boolean}, {@link Character}, {@link Short},
     * {@link Integer}, {@link Long}, {@link Float}, {@link Double},
     * {@link String} or {@link Type}, or an two elements String array (for
     * enumeration values), a {@link AnnotationNode}, or a {@link List} of
     * values of one of the preceding types. May be <tt>null</tt>.
     */
    public Object annotationDefault;

    /**
     * The runtime visible parameter annotations of this method. These lists are
     * lists of {@link AnnotationNode} objects. May be <tt>null</tt>.
     *
     * @associates org.objectweb.asm.tree.AnnotationNode
     * @label invisible parameters
     */
    public List<AnnotationNode>[] visibleParameterAnnotations;

    /**
     * The runtime invisible parameter annotations of this method. These lists
     * are lists of {@link AnnotationNode} objects. May be <tt>null</tt>.
     *
     * @associates org.objectweb.asm.tree.AnnotationNode
     * @label visible parameters
     */
    public List<AnnotationNode>[] invisibleParameterAnnotations;

    /**
     * The instructions of this method. This list is a list of
     * {@link AbstractInsnNode} objects.
     *
     * @associates org.objectweb.asm.tree.AbstractInsnNode
     * @label instructions
     */
    public InsnList instructions;
    
    public List<InstructionNode> instructionNodes = new ArrayList<InstructionNode>();

    /**
     * The try catch blocks of this method. This list is a list of
     * {@link TryCatchBlockNode} objects.
     *
     * @associates org.objectweb.asm.tree.TryCatchBlockNode
     */
    public List<TryCatchBlockNode> tryCatchBlocks;

    /**
     * The maximum stack size of this method.
     */
    public int maxStack;

    /**
     * The maximum number of local variables of this method.
     */
    public int maxLocals;

    /**
     * The local variables of this method. This list is a list of
     * {@link LocalVariableNode} objects. May be <tt>null</tt>
     *
     * @associates org.objectweb.asm.tree.LocalVariableNode
     */
    public List<LocalVariableNode> localVariables;

    /**
     * If the accept method has been called on this object.
     */
    private boolean visited;
    
    /**
     * Constructs an uninitialized {@link MethodNode}. <i>Subclasses must not
     * use this constructor</i>. Instead, they must use the
     * {@link #MethodNode(int)} version.
     */
    public MethodNode() {
        this(Opcodes.ASM4);
    }

    /**
     * Constructs an uninitialized {@link MethodNode}.
     *
     * @param api the ASM API version implemented by this visitor. Must be one
     *        of {@link Opcodes#ASM4}.
     */
    public MethodNode(final int api) {
        super(api);
        this.instructions = new InsnList();
    }

    /**
     * Constructs a new {@link MethodNode}. <i>Subclasses must not use this
     * constructor</i>. Instead, they must use the
     * {@link #MethodNode(int, int, String, String, String, String[])} version.
     *
     * @param access the method's access flags (see {@link Opcodes}). This
     *        parameter also indicates if the method is synthetic and/or
     *        deprecated.
     * @param name the method's name.
     * @param desc the method's descriptor (see {@link Type}).
     * @param signature the method's signature. May be <tt>null</tt>.
     * @param exceptions the internal names of the method's exception classes
     *        (see {@link Type#getInternalName() getInternalName}). May be
     *        <tt>null</tt>.
     */
    public MethodNode(
        final int access,
        final String name,
        final String desc,
        final String signature,
        final String[] exceptions)
    {
        this(Opcodes.ASM4, access, name, desc, signature, exceptions);
    }

    /**
     * Constructs a new {@link MethodNode}.
     *
     * @param api the ASM API version implemented by this visitor. Must be one
     *        of {@link Opcodes#ASM4}.
     * @param access the method's access flags (see {@link Opcodes}). This
     *        parameter also indicates if the method is synthetic and/or
     *        deprecated.
     * @param name the method's name.
     * @param desc the method's descriptor (see {@link Type}).
     * @param signature the method's signature. May be <tt>null</tt>.
     * @param exceptions the internal names of the method's exception classes
     *        (see {@link Type#getInternalName() getInternalName}). May be
     *        <tt>null</tt>.
     */
    public MethodNode(
        final int api,
        final int access,
        final String name,
        final String desc,
        final String signature,
        final String[] exceptions)
    {
        super(api);
        this.access = access;
        this.name = name;
        this.desc = desc;
        this.signature = signature;
        this.exceptions = new ArrayList<String>(exceptions == null
                ? 0
                : exceptions.length);
        boolean isAbstract = (access & Opcodes.ACC_ABSTRACT) != 0;
        if (!isAbstract) {
            this.localVariables = new ArrayList<LocalVariableNode>(5);
        }
        this.tryCatchBlocks = new ArrayList<TryCatchBlockNode>();
        if (exceptions != null) {
            this.exceptions.addAll(Arrays.asList(exceptions));
        }
        this.instructions = new InsnList();
    }

    // ------------------------------------------------------------------------
    // Implementation of the MethodVisitor abstract class
    // ------------------------------------------------------------------------

    @Override
    public AnnotationVisitor visitAnnotationDefault() {
        return new AnnotationNode(new ArrayList<Object>(0) {
            @Override
            public boolean add(final Object o) {
                annotationDefault = o;
                return super.add(o);
            }
        });
    }

    @Override
    public AnnotationVisitor visitAnnotation(
        final String desc,
        final boolean visible)
    {
        AnnotationNode an = new AnnotationNode(desc);
        if (visible) {
            if (visibleAnnotations == null) {
                visibleAnnotations = new ArrayList<AnnotationNode>(1);
            }
            visibleAnnotations.add(an);
        } else {
            if (invisibleAnnotations == null) {
                invisibleAnnotations = new ArrayList<AnnotationNode>(1);
            }
            invisibleAnnotations.add(an);
        }
        return an;
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(
        final int parameter,
        final String desc,
        final boolean visible)
    {
        AnnotationNode an = new AnnotationNode(desc);
        if (visible) {
            if (visibleParameterAnnotations == null) {
                int params = Type.getArgumentTypes(this.desc).length;
                visibleParameterAnnotations = (List<AnnotationNode>[])new List<?>[params];
            }
            if (visibleParameterAnnotations[parameter] == null) {
                visibleParameterAnnotations[parameter] = new ArrayList<AnnotationNode>(1);
            }
            visibleParameterAnnotations[parameter].add(an);
        } else {
            if (invisibleParameterAnnotations == null) {
                int params = Type.getArgumentTypes(this.desc).length;
                invisibleParameterAnnotations = (List<AnnotationNode>[])new List<?>[params];
            }
            if (invisibleParameterAnnotations[parameter] == null) {
                invisibleParameterAnnotations[parameter] = new ArrayList<AnnotationNode>(1);
            }
            invisibleParameterAnnotations[parameter].add(an);
        }
        return an;
    }

    @Override
    public void visitAttribute(final Attribute attr) {
        if (attrs == null) {
            attrs = new ArrayList<Attribute>(1);
        }
        attrs.add(attr);
    }

 
    
    @Override
    public void visitFrame(
        final int type,
        final int nLocal,
        final Object[] local,
        final int nStack,
        final Object[] stack)
    {
        instructions.add(new FrameNode(type, nLocal, local == null
                ? null
                : getLabelNodes(local), nStack, stack == null
                ? null
                : getLabelNodes(stack)));
        InstructionNode i = new InstructionNode();
        i.setType(type);
        i.setnLocal(nLocal);
        i.setLocal(local);
        i.setnStack(nStack);
        i.setStack(stack);
        i.setNodeType("visitFrame");
        instructionNodes.add(i);
    }

    @Override
    public void visitInsn(final int opcode) {
        instructions.add(new InsnNode(opcode));
        InstructionNode i = new InstructionNode();
        i.setOpcode(opcode);
        i.setNodeType("visitInsn");
        instructionNodes.add(i);
    }

    @Override
    public void visitIntInsn(final int opcode, final int operand) {
        instructions.add(new IntInsnNode(opcode, operand));
        InstructionNode i = new InstructionNode();
        i.setOpcode(opcode);
        i.setOperand(operand);
        i.setNodeType("visitIntInsn");
        instructionNodes.add(i);
    }

    @Override
    public void visitVarInsn(final int opcode, final int var) {
        instructions.add(new VarInsnNode(opcode, var));
        InstructionNode i = new InstructionNode();
        i.setOpcode(opcode);
        i.setVar(var);
        i.setNodeType("visitVarInsn");
        instructionNodes.add(i);
    }

    @Override
    public void visitTypeInsn(final int opcode, final String type) {
        instructions.add(new TypeInsnNode(opcode, type));
        InstructionNode i = new InstructionNode();
        i.setOpcode(opcode);
        i.setTypeS(type);
        i.setNodeType("visitTypeInsn");
        instructionNodes.add(i);
    }

    @Override
    public void visitFieldInsn(
        final int opcode,
        final String owner,
        final String name,
        final String desc)
    {
        instructions.add(new FieldInsnNode(opcode, owner, name, desc));
        InstructionNode i = new InstructionNode();
        i.setOpcode(opcode);
        i.setOwner(owner);
        i.setName(name);
        i.setDesc(desc);
        i.setNodeType("visitFieldInsn");
        instructionNodes.add(i);
        
      
        Field f = new Field();
        String name2 = name.replace(Tools.INNER_CLASS_CHARACTER, ".");
        String owner2 = owner.replace(Tools.INNER_CLASS_CHARACTER, ".");
        owner2 = owner2.replace(Tools.cep, ".");
        f.setSimpleName(name2);
        f.setClassFullName(owner2);
        f.setFullName(owner2+"."+name2);
        String type = Type.getType(desc).getClassName();
        f.setType(type);
        fieldInvocations.add(f);
    }

    @Override
    public void visitMethodInsn(
        final int opcode,
        final String owner,
        final String name,
        final String desc)
    {
    	try {
    		
    	
        instructions.add(new MethodInsnNode(opcode, owner, name, desc));
        Method m = new Method();
        m.setSimpleName(name);
        String owner2 = owner.replace(Tools.cep, ".");
        
//        owner2 = Tools.removeDollar(owner2);
//        owner2 = owner2.replace("..", ".");
        
        m.setClassFullName(owner2);
        Type returnType = Type.getReturnType(desc);
//        System.out.println(returnType.getClassName()+"");
        Type[] argumentTypes = Type.getArgumentTypes(desc);
        List<String> parametersList = Tools.getParameters(argumentTypes);
        m.setParameters(parametersList);
        String parameters = Tools.getParametersSignature(parametersList);
//        System.out.println("parameters: "+parameters);
//        parameters = parameters.replace("$", ".");
        m.setParametersSignature(parameters);
        m.setFullName(owner2+"."+name+"("+parameters+")");
        m.setType(returnType.getClassName());
        
      
        methodInvocations.add(m);
        //lembrar que nao estou setando a visibilidade

        InstructionNode i = new InstructionNode();
        i.setOpcode(opcode);
        i.setOwner(owner);
        i.setName(name);
        i.setDesc(desc);
        i.setNodeType("visitMethodInsn");
        instructionNodes.add(i);
    	}catch(Exception e) {
    		System.out.println();
    	}
    }

    @Override
    public void visitInvokeDynamicInsn(
        String name,
        String desc,
        Handle bsm,
        Object... bsmArgs)
    {
        instructions.add(new InvokeDynamicInsnNode(name, desc, bsm, bsmArgs));
        InstructionNode i = new InstructionNode();
        i.setBsm(bsm);
        i.setBsmArgs(bsmArgs);
        i.setName(name);
        i.setDesc(desc);
        i.setNodeType("visitInvokeDynamicInsn");
        instructionNodes.add(i);
    }

    @Override
    public void visitJumpInsn(final int opcode, final Label label) {
        instructions.add(new JumpInsnNode(opcode, getLabelNode(label)));
        InstructionNode i = new InstructionNode();
        i.setOpcode(opcode);
       // i.setLabel(label);
        i.setNodeType("visitJumpInsn");
        instructionNodes.add(i);
    }

    @Override
    public void visitLabel(final Label label) {
        instructions.add(getLabelNode(label));
//        InstructionNode i = new InstructionNode();
//        i.setLabel(label);
//        i.setNodeType("visitLabel");
//        instructionNodes.add(i);
    }

    @Override
    public void visitLdcInsn(final Object cst) {
        instructions.add(new LdcInsnNode(cst));
        InstructionNode i = new InstructionNode();
        i.setCst(cst);
        i.setNodeType("visitLdcInsn");
        instructionNodes.add(i);
    }

    @Override
    public void visitIincInsn(final int var, final int increment) {
        instructions.add(new IincInsnNode(var, increment));
        InstructionNode i = new InstructionNode();
        i.setVar(var);
        i.setIncrement(increment);
        i.setNodeType("visitIincInsn");
        instructionNodes.add(i);
    }

    @Override
    public void visitTableSwitchInsn(
        final int min,
        final int max,
        final Label dflt,
        final Label... labels)
    {
        instructions.add(new TableSwitchInsnNode(min,
                max,
                getLabelNode(dflt),
                getLabelNodes(labels)));
        InstructionNode i = new InstructionNode();
        i.setMin(min);
        i.setMax(max);
//        i.setDflt(dflt);
//        i.setLabels(labels);
        i.setNodeType("visitTableSwitchInsn");
        instructionNodes.add(i);
    }

    @Override
    public void visitLookupSwitchInsn(
        final Label dflt,
        final int[] keys,
        final Label[] labels)
    {
        instructions.add(new LookupSwitchInsnNode(getLabelNode(dflt),
                keys,
                getLabelNodes(labels)));
        InstructionNode i = new InstructionNode();
        i.setKeys(keys);
//        i.setDflt(dflt);
//        i.setLabels(labels);
        i.setNodeType("visitLookupSwitchInsn");
        instructionNodes.add(i);
    }

    @Override
    public void visitMultiANewArrayInsn(final String desc, final int dims) {
        instructions.add(new MultiANewArrayInsnNode(desc, dims));
        InstructionNode i = new InstructionNode();
        i.setDesc(desc);
        i.setDims(dims);
        i.setNodeType("visitMultiANewArrayInsn");
        instructionNodes.add(i);
    }

    @Override
    public void visitTryCatchBlock(
        final Label start,
        final Label end,
        final Label handler,
        final String type)
    {
        tryCatchBlocks.add(new TryCatchBlockNode(getLabelNode(start),
                getLabelNode(end),
                getLabelNode(handler),
                type));
        InstructionNode i = new InstructionNode();
//        i.setStart(start);
//        i.setEnd(end);
//        i.setHandler(handler);
        i.setTypeS(type);
        i.setNodeType("visitTryCatchBlock");
        instructionNodes.add(i);
    }

    @Override
    public void visitLocalVariable(
        final String name,
        final String desc,
        final String signature,
        final Label start,
        final Label end,
        final int index)
    {
        localVariables.add(new LocalVariableNode(name,
                desc,
                signature,
                getLabelNode(start),
                getLabelNode(end),
                index));
        InstructionNode i = new InstructionNode();
       // i.setStart(start);
       // i.setEnd(end);
        i.setSignature(signature);
        i.setNodeType("visitLocalVariable");
        i.setName(name);
        i.setDesc(desc);
        i.setIndex(index);
        instructionNodes.add(i);
        
    }

    @Override
    public void visitLineNumber(final int line, final Label start) {
        instructions.add(new LineNumberNode(line, getLabelNode(start)));
//        InstructionNode i = new InstructionNode();
//        i.setLine(line);
//        i.setStart(start);
//        i.setNodeType("visitLineNumber");
//        instructionNodes.add(i);
    }

    @Override
    public void visitMaxs(final int maxStack, final int maxLocals) {
        this.maxStack = maxStack;
        this.maxLocals = maxLocals;
    }

    @Override
    public void visitEnd() {
    }

    /**
     * Returns the LabelNode corresponding to the given Label. Creates a new
     * LabelNode if necessary. The default implementation of this method uses
     * the {@link Label#info} field to store associations between labels and
     * label nodes.
     *
     * @param l a Label.
     * @return the LabelNode corresponding to l.
     */
    protected LabelNode getLabelNode(final Label l) {
        if (!(l.info instanceof LabelNode)) {
            l.info = new LabelNode(l);
        }
        return (LabelNode) l.info;
    }

    private LabelNode[] getLabelNodes(final Label[] l) {
        LabelNode[] nodes = new LabelNode[l.length];
        for (int i = 0; i < l.length; ++i) {
            nodes[i] = getLabelNode(l[i]);
        }
        return nodes;
    }

    private Object[] getLabelNodes(final Object[] objs) {
        Object[] nodes = new Object[objs.length];
        for (int i = 0; i < objs.length; ++i) {
            Object o = objs[i];
            if (o instanceof Label) {
                o = getLabelNode((Label) o);
            }
            nodes[i] = o;
        }
        return nodes;
    }

    // ------------------------------------------------------------------------
    // Accept method
    // ------------------------------------------------------------------------

    /**
     * Checks that this method node is compatible with the given ASM API
     * version. This methods checks that this node, and all its nodes
     * recursively, do not contain elements that were introduced in more recent
     * versions of the ASM API than the given version.
     *
     * @param api an ASM API version. Must be one of {@link Opcodes#ASM4}.
     */
    public void check(final int api) {
        // nothing to do
    }

    /**
     * Makes the given class visitor visit this method.
     *
     * @param cv a class visitor.
     */
    public void accept(final ClassVisitor cv) {
        String[] exceptions = new String[this.exceptions.size()];
        this.exceptions.toArray(exceptions);
        MethodVisitor mv = cv.visitMethod(access,
                name,
                desc,
                signature,
                exceptions);
        if (mv != null) {
            accept(mv);
        }
    }

    /**
     * Makes the given method visitor visit this method.
     *
     * @param mv a method visitor.
     */
    public void accept(final MethodVisitor mv) {
        // visits the method attributes
        int i, j, n;
        if (annotationDefault != null) {
            AnnotationVisitor av = mv.visitAnnotationDefault();
            AnnotationNode.accept(av, null, annotationDefault);
            if (av != null) {
                av.visitEnd();
            }
        }
        n = visibleAnnotations == null ? 0 : visibleAnnotations.size();
        for (i = 0; i < n; ++i) {
            AnnotationNode an = visibleAnnotations.get(i);
            an.accept(mv.visitAnnotation(an.desc, true));
        }
        n = invisibleAnnotations == null ? 0 : invisibleAnnotations.size();
        for (i = 0; i < n; ++i) {
            AnnotationNode an = invisibleAnnotations.get(i);
            an.accept(mv.visitAnnotation(an.desc, false));
        }
        n = visibleParameterAnnotations == null
                ? 0
                : visibleParameterAnnotations.length;
        for (i = 0; i < n; ++i) {
            List<?> l = visibleParameterAnnotations[i];
            if (l == null) {
                continue;
            }
            for (j = 0; j < l.size(); ++j) {
                AnnotationNode an = (AnnotationNode) l.get(j);
                an.accept(mv.visitParameterAnnotation(i, an.desc, true));
            }
        }
        n = invisibleParameterAnnotations == null
                ? 0
                : invisibleParameterAnnotations.length;
        for (i = 0; i < n; ++i) {
            List<?> l = invisibleParameterAnnotations[i];
            if (l == null) {
                continue;
            }
            for (j = 0; j < l.size(); ++j) {
                AnnotationNode an = (AnnotationNode) l.get(j);
                an.accept(mv.visitParameterAnnotation(i, an.desc, false));
            }
        }
        if (visited) {
            instructions.resetLabels();
        }
        n = attrs == null ? 0 : attrs.size();
        for (i = 0; i < n; ++i) {
            mv.visitAttribute(attrs.get(i));
        }
        // visits the method's code
        if (instructions.size() > 0) {
            mv.visitCode();
            // visits try catch blocks
            n = tryCatchBlocks == null ? 0 : tryCatchBlocks.size();
            for (i = 0; i < n; ++i) {
                tryCatchBlocks.get(i).accept(mv);
            }
            // visits instructions
            instructions.accept(mv);
            // visits local variables
            n = localVariables == null ? 0 : localVariables.size();
            for (i = 0; i < n; ++i) {
                localVariables.get(i).accept(mv);
            }
            // visits maxs
            mv.visitMaxs(maxStack, maxLocals);
            visited = true;
        }
        mv.visitEnd();
    }

	public List<InstructionNode> getInstructionNodes() {
		return instructionNodes;
	}

	public void setInstructionNodes(List<InstructionNode> instructionNodes) {
		this.instructionNodes = instructionNodes;
	}
}
