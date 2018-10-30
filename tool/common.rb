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
SCALA_SOURCES = Dir[BOOTCOMPILER / "src/**/*.scala"]
BOOTCOMPILER_CLASSES = BOOTCOMPILER / "target/scala-2.11/classes"

MX = Pathname("../mx/mx").expand_path(dir)
GRAAL_REPO = Pathname("../graal").expand_path(dir)

JVMCI = Pathname("../jvmci").expand_path(dir)
GRAAL = GRAAL_REPO / "compiler"
GRAAL_JAR = GRAAL / "mxbuild/dists/graal.jar"

SDK = GRAAL_REPO / "sdk"
SDK_JAR = SDK / "mxbuild/dists/graal-sdk.jar"
SDK_SRC = SDK / "mxbuild/dists/graal-sdk.src.zip"
LAUNCHER_COMMON_JAR = SDK / "mxbuild/dists/launcher-common.jar"

TRUFFLE = GRAAL_REPO / "truffle"
TRUFFLE_API_JAR = TRUFFLE / "mxbuild/dists/truffle-api.jar"
TRUFFLE_API_SRC = TRUFFLE / "mxbuild/dists/truffle-api.src.zip"
TRUFFLE_DSL_PROCESSOR_JAR = TRUFFLE / "mxbuild/dists/truffle-dsl-processor.jar"

TOOLS = GRAAL_REPO / "tools"
PROFILER_JAR = TOOLS / "mxbuild/dists/truffle-profiler.jar"
INSPECTOR_JAR = TOOLS / "mxbuild/dists/chromeinspector.jar"

VM = PROJECT_DIR / "vm"
JAVA_SOURCES = Dir["#{VM}/**/*.java"]
REFLECTION_JSON = "#{VM}/src/org/mozartoz/truffle/reflection.json"

PROJECT_JAR = PROJECT_DIR / "mxbuild/dists/jdk1.8/mozart-graal.jar"
LAUNCHER_JAR = PROJECT_DIR / "mxbuild/dists/jdk1.8/mozart-graal-launcher.jar"

MAIN_IMAGE = PROJECT_DIR / "Main.image"

def oz_classpath
  [
    PROJECT_JAR,
    LAUNCHER_JAR,
    PROFILER_JAR,
    INSPECTOR_JAR,
  ]
end
