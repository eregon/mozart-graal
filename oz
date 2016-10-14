#!/usr/bin/env ruby

require_relative 'tool/common'

dir = File.expand_path('..', __FILE__)

if MAIN_IMAGE.exist? and mtime = MAIN_IMAGE.mtime and
    f = (JAVA_SOURCES + SCALA_SOURCES).find { |src| File.mtime(src) > mtime }
  $stderr.puts "Removing Main.image because #{File.basename(f)} is more recent"
  MAIN_IMAGE.delete
end

bootclasspath = [TRUFFLE_API_JAR]
classpath = oz_classpath

java = 'java'
java_opts = %w[-ea -esa]
vm_options = []

args = ARGV.drop_while { |arg|
  if arg.start_with? '-'
    vm_options << arg
  end
}

if vm_options.delete('--graal')
  jvmci_home = File.read("../graal-core/mx.graal-core/env").scan(/^JAVA_HOME=(.+)/)[0][0]
  java = File.expand_path("#{jvmci_home}/bin/java")
  java_opts += %w[-server -XX:+UnlockExperimentalVMOptions -XX:+EnableJVMCI -d64]
  java_opts << "-Djvmci.class.path.append=#{GRAAL_JAR}"
  java_opts << "-Djvmci.Compiler=graal"
end

if vm_options.delete('--igv')
  java_opts << "-Dgraal.Dump=TruffleTree,PartialEscape"
  java_opts << "-Dgraal.PrintBackendCFG=false"
  java_opts << "-Dgraal.TruffleBackgroundCompilation=false"
end

if vm_options.delete('--infopoints')
  java_opts << "-XX:+UnlockDiagnosticVMOptions" << "-XX:+DebugNonSafepoints"
  java_opts << "-Dgraal.TruffleEnableInfopoints=true"
end

cmd = [
  java,
  *java_opts,
  *vm_options,
  "-Xbootclasspath/p:" + bootclasspath.join(':'),
  '-cp', classpath.join(':'),
  'org.mozartoz.truffle.Main'
] + args

puts "$ #{cmd.join(' ')}"
exec(*cmd)
