import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import java.nio.file.Path

val RegularFile.asPath: Path
    get() = this.asFile.toPath()

val Directory.asPath: Path
    get() = this.asFile.toPath()
