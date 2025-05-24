package red.vuis.vbm.proposal;

public interface ProposalCollector {
    void collectField(String className, String fieldName, String fieldDesc, String target);

    default void finished() {
    }
}
