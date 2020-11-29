function initializeCoreMod() {

    Opcodes = Java.type("org.objectweb.asm.Opcodes");

    MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
    InsnList = Java.type("org.objectweb.asm.tree.InsnList");
    LdcInsnNode = Java.type("org.objectweb.asm.tree.LdcInsnNode");
    FieldInsnNode = Java.type("org.objectweb.asm.tree.FieldInsnNode");

    return {
        'MinecraftServer#func_240802_v_': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.server.MinecraftServer',
                'methodName': 'func_228401_a_',
                'methodDesc': '()V'
            },
            'transformer': function (methodNode) {
                var list = new InsnList();
                for (var i = 0; i < instructions.size(); i++) {
                    var instruction = instructions.get(i);
                    if (instruction instanceof LdcInsnNode) {
                        if (instruction.cst instanceof Long && instruction.cst == 50) {
       		                list.add(new FieldInsnNode(Opcodes.INVOKESTATIC, "chappie/theboys/util/TBUtil", "MILISECONDS_PER_TICK", "J"));
                            continue;
                        }
                    }
                list.add(node);
                }
                method.instructions.clear();
                method.instructions.add(list);
                return methodNode;
            }
        }
    }
}
