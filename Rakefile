require 'pathname'

MOZART2 = Pathname("../mozart2").expand_path

OZWISH = MOZART2 / "wish/ozwish"
OZWISH_SRC = MOZART2 / "wish/unixmain.cc"

BOOTCOMPILER = MOZART2 / "bootcompiler"
BOOTCOMPILER_JAR = BOOTCOMPILER / "target/scala-2.11/bootcompiler-assembly-2.0-SNAPSHOT.jar"
BOOTCOMPILER_ECLIPSE = BOOTCOMPILER / ".project"

MX = Pathname("../mx/mx").expand_path

JVMCI = Pathname("../jvmci")
GRAAL = Pathname("../graal-coro")
GRAAL_JAR = GRAAL / "mxbuild/dists/graal-truffle.jar"

TRUFFLE = Pathname("../truffle").expand_path
TRUFFLE_API_JAR = TRUFFLE / "mxbuild/dists/truffle-api.jar"
TRUFFLE_API_SRC = TRUFFLE / "mxbuild/dists/truffle-api.src.zip"
TRUFFLE_DSL_PROCESSOR_JAR = TRUFFLE / "mxbuild/dists/truffle-dsl-processor.jar"

MAIN_CLASS = Pathname("bin/org/mozartoz/truffle/Main.class")
JAVA_SOURCES = Dir["src/**/*.java"]

namespace :build do
  task :all => [:truffle, :mozart2, :bootcompiler, :project]

  task :mozart2 => [MOZART2, OZWISH]
  task :bootcompiler => [:mozart2, BOOTCOMPILER_JAR, BOOTCOMPILER_ECLIPSE]

  task :truffle => TRUFFLE_API_JAR

  desc "Build Graal"
  task :graal => GRAAL_JAR

  task :project => [:javac, ".classpath", ".factorypath"]
  task :javac => MAIN_CLASS

  file MOZART2 do
    sh "cd .. && git clone https://github.com/eregon/mozart2.git"
    sh "cd #{MOZART2} && git checkout mozart-graal"
  end

  file OZWISH => OZWISH_SRC do
    sh "cc -o #{OZWISH} -ltcl -ltk #{OZWISH_SRC}"
  end

  file BOOTCOMPILER_JAR => Dir[BOOTCOMPILER / "src/**/*.scala"] do
    sh "cd #{BOOTCOMPILER} && ./sbt assembly"
    touch "#{BOOTCOMPILER_JAR}" # sbt might not update mtime
  end

  file BOOTCOMPILER_ECLIPSE do
    sh "cd #{BOOTCOMPILER} && ./sbt eclipse eclipse-with-source"
  end

  file MX do
    sh "cd .. && git clone https://github.com/graalvm/mx.git"
  end

  file TRUFFLE do
    sh "cd .. && git clone https://github.com/eregon/truffle.git"
    sh "cd #{TRUFFLE} && git checkout coro"
  end

  file TRUFFLE_API_JAR => [MX, TRUFFLE] do
    sh "cd #{TRUFFLE} && #{MX} build"
  end
  file TRUFFLE_DSL_PROCESSOR_JAR => TRUFFLE_API_JAR

  file JVMCI do
    sh "cd .. && git clone https://github.com/eregon/jvmci.git"
    sh "cd #{JVMCI} && git checkout coro"
  end

  file GRAAL => [TRUFFLE, JVMCI] do
    sh "cd .. && git clone https://github.com/eregon/graal-core.git #{GRAAL}"
    sh "cd #{GRAAL} && git checkout coro"
  end

  file GRAAL_JAR => [GRAAL, MX] do
    sh "cd #{GRAAL} && #{MX} --vm server build"
  end

  file ".classpath" do
    require 'erb'
    File.write '.classpath', ERB.new(File.read('tool/classpath.erb')).result(binding)
  end

  file ".factorypath" do
    require 'erb'
    File.write '.factorypath', ERB.new(File.read('tool/factorypath.erb')).result(binding)
  end

  directory "bin"

  file MAIN_CLASS => ["bin", TRUFFLE_API_JAR, TRUFFLE_DSL_PROCESSOR_JAR, BOOTCOMPILER_JAR, *JAVA_SOURCES] do
    sh *["javac", "-cp", "#{TRUFFLE_API_JAR}:#{TRUFFLE_DSL_PROCESSOR_JAR}:#{BOOTCOMPILER_JAR}:bin", "-sourcepath", "src", "-d", "bin", *JAVA_SOURCES]
  end
end

desc "Build the project and dependencies"
task :build => "build:all"

desc "Run tests"
task :test do
  exec './oz'
end

task :default => [:build, :test]

desc "Update all repositories"
task :up do
  [".", MOZART2, TRUFFLE, JVMCI, GRAAL].each { |dir|
    if File.directory?(dir)
      sh "cd #{dir} && git pull"
    end
  }
end
