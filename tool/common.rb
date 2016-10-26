require 'pathname'

class Pathname
  alias :/ :+ unless method_defined?(:/)

  def write(contents)
    open('w') do |f|
      f.write contents
    end
  end unless method_defined?(:write)
end

PROJECT_DIR = dir = Pathname(File.expand_path('../..', __FILE__))

BOOTCOMPILER = PROJECT_DIR / "bootcompiler"
BOOTCOMPILER_JAR = BOOTCOMPILER / "target/scala-2.11/bootcompiler-assembly-2.0-SNAPSHOT.jar"
BOOTCOMPILER_CLASSES = BOOTCOMPILER / "bin"
SCALA_SOURCES = Dir[BOOTCOMPILER / "src/**/*.scala"]

MX = Pathname("../mx/mx").expand_path(dir)

JVMCI = Pathname("../jvmci").expand_path(dir)
GRAAL = Pathname("../graal-core").expand_path(dir)
GRAAL_JAR = GRAAL / "mxbuild/dists/graal.jar"

TRUFFLE = Pathname("../truffle").expand_path(dir)
TRUFFLE_API_JAR = TRUFFLE / "mxbuild/dists/truffle-api.jar"

VM = PROJECT_DIR / "vm"
VM_CLASSES = (VM / "bin").to_s
MAIN_CLASS = VM / "bin/org/mozartoz/truffle/Main.class"
JAVA_SOURCES = Dir["#{VM}/src/**/*.java"]

MAIN_IMAGE = PROJECT_DIR / "Main.image"

def maven_classpath
  (VM / ".classpath").read.scan(%r{kind="lib" path="([^"]+/\.m2/repository/[^"]+)"}).map(&:first)
end

def oz_classpath
  cp = []
  cp << VM_CLASSES
  newest_bootcompiler_class = Dir[BOOTCOMPILER_CLASSES / "**/*.class"].max_by { |f| File.mtime(f) }
  if newest_bootcompiler_class and File.mtime(newest_bootcompiler_class) > BOOTCOMPILER_JAR.mtime
    cp << BOOTCOMPILER_CLASSES
  end
  cp << BOOTCOMPILER_JAR
  cp + maven_classpath
end
