package red.vuis.vbm.proposal;

public interface ProposalCollector {
    void collectField(String className, String fieldName, String fieldDesc, String target);

    void collectMethod(String className, String methodName, String methodDesc, String target);
    void collectMethodArg(int index, int slot, String target);

    default void finished() {
    }
}
