package red.vuis.vbm.proposal.visitor;

import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;
import red.vuis.vbm.proposal.ProposalCollector;
import red.vuis.vbm.util.VbmUtils;

public class PacketVisitor extends ProposalVisitor {
    public static final String CUSTOM_PACKET_PAYLOAD = "net/minecraft/network/protocol/common/custom/CustomPacketPayload";

    private static final String TYPE_DESCRIPTOR = "Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload$Type;";
    private static final String CODEC_DESCRIPTOR = "Lnet/minecraft/network/codec/StreamCodec;";
    private static final String PAYLOAD_CONTEXT_NAME = "net/neoforged/neoforge/network/handling/IPayloadContext";

    public PacketVisitor(ProposalCollector collector) {
        super(collector);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        if (VbmUtils.allBits(access, Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL)) {
            switch (descriptor) {
                case TYPE_DESCRIPTOR -> collector.collectField(className, name, descriptor, "TYPE");
                case CODEC_DESCRIPTOR -> collector.collectField(className, name, descriptor, "CODEC");
            }
        }
        return super.visitField(access, name, descriptor, signature, value);
    }

//    @Override
//    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
//        Type methodType = Type.getMethodType(descriptor);
//        Type[] argumentTypes = methodType.getArgumentTypes();
//        Type returnType = methodType.getReturnType();
//
//        if (VbmUtils.allBits(access, Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC) &&
//                argumentTypes.length == 2 &&
//                argumentTypes[0].getInternalName().equals(className) &&
//                argumentTypes[1].getInternalName().equals(PAYLOAD_CONTEXT_NAME) &&
//                returnType.getInternalName().equals("V")
//        ) {
//            collector.collectMethod(className, name, descriptor, "apply");
//            collector.collectMethodArg(0, 0, "packet");
//            collector.collectMethodArg(1, 1, "context");
//        }
//
//        return super.visitMethod(access, name, descriptor, signature, exceptions);
//    }
}
