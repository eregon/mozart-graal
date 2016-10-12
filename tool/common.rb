require 'pathname'

class Pathname
  alias :/ :+ unless method_defined?(:/)
end

PROJECT_DIR = dir = Pathname(File.expand_path('../..', __FILE__))

MOZART2 = Pathname("../mozart2").expand_path(dir)

BOOTCOMPILER = MOZART2 / "bootcompiler"
BOOTCOMPILER_JAR = BOOTCOMPILER / "target/scala-2.11/bootcompiler-assembly-2.0-SNAPSHOT.jar"

MX = Pathname("../mx/mx").expand_path(dir)

JVMCI = Pathname("../jvmci").expand_path(dir)
GRAAL = Pathname("../graal-core").expand_path(dir)
GRAAL_JAR = GRAAL / "mxbuild/dists/graal.jar"

TRUFFLE = Pathname("../truffle").expand_path(dir)
TRUFFLE_API_JAR = TRUFFLE / "mxbuild/dists/truffle-api.jar"

JAVA_SOURCES = Dir["#{dir}/src/**/*.java"]
MAIN_CLASS = Pathname("bin/org/mozartoz/truffle/Main.class").expand_path(dir)

MAIN_IMAGE = PROJECT_DIR / "Main.image"

def oz_classpath
  maven_classpath = File.read("#{PROJECT_DIR}/.classpath").scan(%r{kind="lib" path="([^"]+/\.m2/repository/[^"]+)"}).map(&:first)
  rel = PROJECT_DIR == Pathname.pwd ? "" : "#{PROJECT_DIR}/"
  ["#{rel}bin", BOOTCOMPILER_JAR] + maven_classpath
end
