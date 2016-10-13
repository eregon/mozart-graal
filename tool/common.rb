require 'pathname'

class Pathname
  alias :/ :+ unless method_defined?(:/)
end

PROJECT_DIR = dir = Pathname(File.expand_path('../..', __FILE__))

BOOTCOMPILER = PROJECT_DIR / "bootcompiler"
BOOTCOMPILER_JAR = BOOTCOMPILER / "target/scala-2.11/bootcompiler-assembly-2.0-SNAPSHOT.jar"

MX = Pathname("../mx/mx").expand_path(dir)

JVMCI = Pathname("../jvmci").expand_path(dir)
GRAAL = Pathname("../graal-core").expand_path(dir)
GRAAL_JAR = GRAAL / "mxbuild/dists/graal.jar"

TRUFFLE = Pathname("../truffle").expand_path(dir)
TRUFFLE_API_JAR = TRUFFLE / "mxbuild/dists/truffle-api.jar"

VM = PROJECT_DIR / "vm"
VM_CLASSES = (VM / "bin").to_s
JAVA_SOURCES = Dir["#{VM}/src/**/*.java"]
MAIN_CLASS = VM / "bin/org/mozartoz/truffle/Main.class"

MAIN_IMAGE = PROJECT_DIR / "Main.image"

def oz_classpath
  maven_classpath = (VM / ".classpath").read.scan(%r{kind="lib" path="([^"]+/\.m2/repository/[^"]+)"}).map(&:first)
  [VM_CLASSES, BOOTCOMPILER_JAR] + maven_classpath
end
