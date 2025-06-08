import net.fabricmc.mappingio.MappingWriter
import net.fabricmc.mappingio.format.MappingFormat
import net.fabricmc.mappingio.format.enigma.EnigmaDirReader
import net.fabricmc.mappingio.tree.MappingTree
import net.fabricmc.mappingio.tree.MemoryMappingTree
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

abstract class FormatMappingsTask : DefaultTask() {
    @get:InputDirectory
    abstract val input: DirectoryProperty
    @get:OutputDirectory
    abstract val output: DirectoryProperty

    @TaskAction
    fun run() {
        val tree = MemoryMappingTree()
        EnigmaDirReader.read(input.get().asPath, "intermediary", "named", tree)
        val namedId = tree.getNamespaceId("named")

        val fieldsToRemove = mutableListOf<MappingTree.FieldMapping>()
        val methodsToRemove = mutableListOf<MappingTree.MethodMapping>()

        for (treeClass in tree.classes) {
            for (treeField in treeClass.fields) {
                if (treeField.srcName == treeField.getDstName(namedId) || !treeField.srcName.startsWith("field_")) {
                    treeField.setDstName(null, namedId)
                    if (treeField.comment == null) {
                        fieldsToRemove.add(treeField)
                    }
                }
            }
            for (treeMethod in treeClass.methods) {
                if (treeMethod.srcName == treeMethod.getDstName(namedId) || !treeMethod.srcName.startsWith("method_")) {
                    treeMethod.setDstName(null, namedId)
                    if (treeMethod.args.isEmpty() && treeMethod.comment == null) {
                        methodsToRemove.add(treeMethod)
                    }
                }
            }
        }

        val classesToRemove = mutableListOf<String>()

        for (fieldToRemove in fieldsToRemove) {
            val owner = fieldToRemove.owner
            owner.removeField(fieldToRemove.srcName, fieldToRemove.srcDesc)
            if (isClassEmpty(owner, namedId)) {
                classesToRemove.add(owner.srcName)
            }
        }
        for (methodToRemove in methodsToRemove) {
            val owner = methodToRemove.owner
            owner.removeMethod(methodToRemove.srcName, methodToRemove.srcDesc)
            if (isClassEmpty(owner, namedId)) {
                classesToRemove.add(owner.srcName)
            }
        }

        for (classToRemove in classesToRemove) {
            tree.removeClass(classToRemove)
        }

        tree.accept(MappingWriter.create(output.get().asPath, MappingFormat.ENIGMA_DIR))
    }
}

fun isClassEmpty(treeClass: MappingTree.ClassMapping, ns: Int) =
    treeClass.getDstName(ns) == null &&
            treeClass.fields.isEmpty() &&
            treeClass.methods.isEmpty() &&
            treeClass.comment == null
