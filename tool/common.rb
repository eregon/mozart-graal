require 'pathname'

class Pathname
  alias :/ :+ unless method_defined?(:/)
end

PROJECT_DIR = dir = Pathname(File.expand_path('../..', __FILE__))

v = "20-b04"
OPENJDK_JVMCI_URL = "https://github.com/graalvm/openjdk8-jvmci-builder/releases/download/jvmci-#{v}/openjdk-8u212-jvmci-#{v}-linux-amd64.tar.gz"
OPENJDK_JVMCI_ARCHIVE = PROJECT_DIR / ".." / File.basename(OPENJDK_JVMCI_URL)
OPENJDK_JVMCI_HOME = PROJECT_DIR / ".." / "openjdk1.8.0_212-jvmci-#{v}"

BOOTCOMPILER = PROJECT_DIR / "bootcompiler"
BOOTCOMPILER_JAR = BOOTCOMPILER / "target/scala-2.11/bootcompiler-assembly-2.0-SNAPSHOT.jar"
SCALA_SOURCES = Dir[BOOTCOMPILER / "src/**/*.scala"]
BOOTCOMPILER_CLASSES = BOOTCOMPILER / "target/scala-2.11/classes"

MX = Pathname("../mx/mx").expand_path(dir)
GRAAL_REPO = Pathname("../graal").expand_path(dir)

JVMCI = Pathname("../jvmci").expand_path(dir)

dists = "mxbuild/dists/jdk1.8"

GRAAL = GRAAL_REPO / "compiler"
GRAAL_JAR = GRAAL / "#{dists}/graal.jar"

SDK = GRAAL_REPO / "sdk"
SDK_JAR = SDK / "#{dists}/graal-sdk.jar"
SDK_SRC = SDK / "#{dists}/graal-sdk.src.zip"
LAUNCHER_COMMON_JAR = SDK / "#{dists}/launcher-common.jar"

TRUFFLE = GRAAL_REPO / "truffle"
TRUFFLE_API_JAR = TRUFFLE / "#{dists}/truffle-api.jar"

TOOLS = GRAAL_REPO / "tools"
PROFILER_JAR = TOOLS / "#{dists}/truffle-profiler.jar"
INSPECTOR_JAR = TOOLS / "#{dists}/chromeinspector.jar"

VM_DIR = PROJECT_DIR / "vm"
JAVA_SOURCES = Dir["#{VM_DIR}/**/*.java"]
REFLECTION_JSON = "#{VM_DIR}/src/org/mozartoz/truffle/reflection.json"

CORO_JAR = PROJECT_DIR / "#{dists}/coro.jar"
PROJECT_JAR = PROJECT_DIR / "#{dists}/mozart-graal.jar"
LAUNCHER_JAR = PROJECT_DIR / "#{dists}/mozart-graal-launcher.jar"

MAIN_IMAGE = PROJECT_DIR / "Main.image"

def oz_bootclasspath
  [
    SDK_JAR,
    LAUNCHER_COMMON_JAR,
    TRUFFLE_API_JAR
  ]
end

def oz_classpath
  [
    CORO_JAR,
    PROJECT_JAR,
    LAUNCHER_JAR,
    PROFILER_JAR,
    INSPECTOR_JAR,
  ]
end
