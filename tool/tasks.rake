require_relative 'common'
require 'tempfile'
require 'json'

TRUFFLE_RELEASE = "1.0.0-rc10"
JVMCI_BASE = "1.8.0_121"
MX_TAG = "5.224.1"

JDK8_ARCHIVE = "http://www.oracle.com/technetwork/java/javase/downloads/java-archive-javase8-2177648.html"

v = "20-b04"
OPENJDK_JVMCI_URL = "https://github.com/graalvm/openjdk8-jvmci-builder/releases/download/jvmci-#{v}/openjdk-8u212-jvmci-#{v}-linux-amd64.tar.gz"
OPENJDK_JVMCI_ARCHIVE = PROJECT_DIR / ".." / File.basename(OPENJDK_JVMCI_URL)
OPENJDK_JVMCI_DIR = PROJECT_DIR / ".." / "openjdk1.8.0_212-jvmci-#{v}"

OZWISH = PROJECT_DIR / "wish/ozwish"
OZWISH_SRC = PROJECT_DIR / "wish/unixmain.cc"

JVMCI_HOME = JVMCI / "jdk#{JVMCI_BASE}/product"
JVMCI_RELEASE = JVMCI_HOME / "release"

namespace :build do
  task :all => [:ozwish, :stdlib, :project]

  task :bootcompiler => BOOTCOMPILER_JAR
  task :ozwish => OZWISH

  desc "Build Graal"
  task :graal => GRAAL_JAR

  task :project => [:javac]
  task :javac => [PROJECT_JAR, LAUNCHER_JAR]

  task :stdlib => "stdlib/README"

  task :graalvm => "graalvm"

  file "stdlib/README" do
    sh "git submodule update --init"
  end

  file OZWISH => OZWISH_SRC do
    flags = if RUBY_PLATFORM.include? "darwin"
      "-I/opt/X11/include"
    else
      ""
    end
    begin
      sh "cc -o #{OZWISH} -ltcl -ltk #{flags} #{OZWISH_SRC}"
    rescue
      puts "WARNING: Failed to build ozwish"
      touch OZWISH
    end
  end

  file BOOTCOMPILER_JAR => SCALA_SOURCES do
    sh "cd #{BOOTCOMPILER} && ./sbt assembly"
    touch BOOTCOMPILER_JAR # sbt might not update mtime
  end

  file MX do
    sh "cd .. && git clone --branch #{MX_TAG} https://github.com/graalvm/mx.git"
  end

  file JVMCI do
    sh "cd .. && git clone https://github.com/eregon/jvmci.git"
    sh "cd #{JVMCI} && git checkout coro-#{TRUFFLE_RELEASE}"
  end

  file JVMCI_RELEASE => [JVMCI, MX] do
    puts "Choose JDK #{JVMCI_BASE} when asked for JAVA_HOME"
    puts "Download it from #{JDK8_ARCHIVE}"
    puts
    sh "cd #{JVMCI} && #{MX} build" unless JVMCI_RELEASE.exist?
    sh "cd #{JVMCI_HOME} && bin/java -version"
  end

  file GRAAL_JAR => :project do
    sh "cd #{GRAAL} && #{MX} --java-home #{JVMCI_HOME} build"
  end

  file PROJECT_JAR => [BOOTCOMPILER_JAR, MX, *JAVA_SOURCES, REFLECTION_JSON] do
    sh "cd #{PROJECT_DIR} && #{MX} build"
  end
  file LAUNCHER_JAR => PROJECT_JAR

  file REFLECTION_JSON => BOOTCOMPILER_JAR do
    config = Dir.chdir(BOOTCOMPILER_CLASSES) do
      Dir["org/mozartoz/bootcompiler/ast/TreeDSL$$*.class"].sort.map do |file|
        java_class = file.sub(/\.class$/, '').tr('/', '.')
        { name: java_class, allPublicMethods: true }
      end
    end
    File.write(REFLECTION_JSON, JSON.pretty_generate(config))
  end

  file OPENJDK_JVMCI_ARCHIVE do
    sh "wget -O #{OPENJDK_JVMCI_ARCHIVE} #{OPENJDK_JVMCI_URL}"
  end

  file OPENJDK_JVMCI_DIR => OPENJDK_JVMCI_ARCHIVE do
    sh "cd .. && tar xf #{OPENJDK_JVMCI_ARCHIVE}"
  end

  desc "Build a GraalVM with a Mozart-Graal native image"
  file "graalvm" => [:project, OPENJDK_JVMCI_DIR] do
    build = [
      MX,
      "--java-home", OPENJDK_JVMCI_DIR,
      "--disable-polyglot", "--disable-libpolyglot",
      "--force-bash-launchers=native-image",
      "--dynamicimports", "mozart-graal,/substratevm",
      "build"
    ]
    sh "cd #{GRAAL_REPO}/vm && #{build.join(' ')}"

    graalvm = "../graal/vm/latest_graalvm_home/jre/languages/oz"
    rm_f "graalvm"
    File.symlink(graalvm, "graalvm")
  end
end

desc "Build the project and dependencies"
task :build => "build:all"

desc "Generate config files for IntelliJ"
task :ideinit => MX do
  # Remove SBT dependencies to avoid mx wondering if they are unused
  rm_f Dir.glob(".idea/libraries/sbt_*.xml")
  sh "#{MX} intellijinit"
  # Restore SBT dependencies
  sh "git checkout .idea/libraries"
  sh "ruby tool/intellijinit.rb"
end

task :clean do
  rm_rf BOOTCOMPILER / 'target'

  rm_rf "mxbuild"
end

desc "Run tests"
task :test do
  exec './oz'
end

task :default => [:build, :test]

desc "Update all repositories"
task :up do
  [".", TRUFFLE, JVMCI, GRAAL].each { |dir|
    if File.directory?(dir)
      sh "cd #{dir} && git pull"
    end
  }
end
