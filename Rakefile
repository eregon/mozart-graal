require_relative 'tool/common'
require 'tempfile'

TRUFFLE_RELEASE = "0.25"
JVMCI_BASE = "1.8.0_121"
MX_COMMIT = "fa891014d1f4a4a647641570406add177e031fc6"

JDK8_ARCHIVE = "http://www.oracle.com/technetwork/java/javase/downloads/java-archive-javase8-2177648.html"

OZWISH = PROJECT_DIR / "wish/ozwish"
OZWISH_SRC = PROJECT_DIR / "wish/unixmain.cc"

BOOTCOMPILER_ECLIPSE = BOOTCOMPILER / ".project"

TRUFFLE_API_SRC = TRUFFLE / "mxbuild/dists/truffle-api.src.zip"
TRUFFLE_DEBUG_SRC = TRUFFLE / "mxbuild/dists/truffle-debug.src.zip"
TRUFFLE_DSL_PROCESSOR_JAR = TRUFFLE / "mxbuild/dists/truffle-dsl-processor.jar"

JVMCI_HOME = JVMCI / "jdk#{JVMCI_BASE}/product"
JVMCI_RELEASE = JVMCI_HOME / "release"
GRAAL_MX_ENV = GRAAL / "mx.graal-core/env"

def erb(template, output)
  require 'erb'
  File.write output, ERB.new(File.read(template), nil, '<>').result(binding)
end

namespace :build do
  task :all => [:truffle, :bootcompiler, :ozwish, :stdlib, :project]

  task :bootcompiler => [BOOTCOMPILER_JAR, BOOTCOMPILER_ECLIPSE]
  task :ozwish => OZWISH

  task :truffle => TRUFFLE_API_JAR

  desc "Build Graal"
  task :graal => GRAAL_JAR

  task :project => [:javac, "vm/.classpath", "vm/.factorypath"]
  task :javac => MAIN_CLASS

  task :stdlib => "stdlib/README"

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
    end
  end

  file BOOTCOMPILER_JAR => SCALA_SOURCES + [TRUFFLE_API_JAR] do
    sh "cd #{BOOTCOMPILER} && ./sbt assembly"
    touch BOOTCOMPILER_JAR # sbt might not update mtime
  end

  file BOOTCOMPILER_ECLIPSE do
    sh "cd #{BOOTCOMPILER} && ./sbt eclipse eclipse-with-source"
    # Export the Scala stdlib to mozart-graal
    classpath = (BOOTCOMPILER / ".classpath")
    classpath.write classpath.read.sub!(
      /(<classpathentry) (kind="con" path=".*SCALA_CONTAINER)/,
      '\1 exported="true" \2')
  end

  file MX do
    sh "cd .. && git clone https://github.com/graalvm/mx.git"
    sh "cd ../mx && git checkout #{MX_COMMIT}"
  end

  file TRUFFLE do
    sh "cd .. && git clone https://github.com/eregon/truffle.git"
    sh "cd #{TRUFFLE} && git checkout coro-#{TRUFFLE_RELEASE}"
  end

  file TRUFFLE_API_JAR => [MX, TRUFFLE] do
    sh "cd #{TRUFFLE} && #{MX} build"
  end
  file TRUFFLE_DSL_PROCESSOR_JAR => TRUFFLE_API_JAR

  file JVMCI do
    sh "cd .. && git clone https://github.com/eregon/jvmci.git"
    sh "cd #{JVMCI} && git checkout coro-#{TRUFFLE_RELEASE}"
  end

  file GRAAL => TRUFFLE do
    sh "git clone https://github.com/eregon/graal-core.git #{GRAAL}" unless GRAAL.directory?
    sh "cd #{GRAAL} && git checkout coro-#{TRUFFLE_RELEASE}"
  end

  file JVMCI_RELEASE => [JVMCI, MX] do
    puts "Choose JDK #{JVMCI_BASE} when asked for JAVA_HOME"
    puts "Download it from #{JDK8_ARCHIVE}"
    puts
    sh "cd #{JVMCI} && #{MX} build" unless JVMCI_RELEASE.exist?
    sh "cd #{JVMCI_HOME} && bin/java -version"
  end

  file GRAAL_MX_ENV => JVMCI_RELEASE do
    GRAAL_MX_ENV.write("JAVA_HOME=#{JVMCI_HOME}\n") unless GRAAL_MX_ENV.exist?
  end

  file GRAAL_JAR => [GRAAL, MX, GRAAL_MX_ENV] do
    sh "cd #{GRAAL} && #{MX} build"
  end

  file "vm/.classpath" => "tool/classpath.erb" do
    sh "cd vm && mvn dependency:build-classpath"
    erb 'tool/classpath.erb', 'vm/.classpath'
  end

  file "vm/.factorypath" => "tool/factorypath.erb" do
    erb 'tool/factorypath.erb', 'vm/.factorypath'
  end

  directory VM_CLASSES

  file MAIN_CLASS => [VM_CLASSES, TRUFFLE_API_JAR, TRUFFLE_DSL_PROCESSOR_JAR, BOOTCOMPILER_JAR, "vm/.classpath", *JAVA_SOURCES] do
    Tempfile.open("sources") do |f|
      f.puts JAVA_SOURCES
      f.close
      sh "javac", "-cp", "#{TRUFFLE_API_JAR}:#{TRUFFLE_DSL_PROCESSOR_JAR}:#{BOOTCOMPILER_JAR}:#{maven_classpath.join(':')}", "-d", VM_CLASSES, "@#{f.path}"
    end
  end
end

desc "Build the project and dependencies"
task :build => "build:all"

task :clean do
  rm_rf BOOTCOMPILER / 'target'
  rm_rf BOOTCOMPILER_ECLIPSE
  rm_rf BOOTCOMPILER / ".classpath"

  rm_rf VM_CLASSES
  rm_rf "vm/.classpath"
  rm_rf "vm/.factorypath"
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
