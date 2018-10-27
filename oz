#!/usr/bin/env ruby

require_relative 'tool/common'

dir = File.expand_path('..', __FILE__)

OZ_MAIN_SOURCES = Dir["lib/main/**/*.oz"]
ALL_SOURCES = JAVA_SOURCES + SCALA_SOURCES + OZ_MAIN_SOURCES

if MAIN_IMAGE.exist? and mtime = MAIN_IMAGE.mtime and
    f = ALL_SOURCES.find { |src| File.mtime(src) > mtime }
  $stderr.puts "Removing Main.image because #{File.basename(f)} is more recent"
  MAIN_IMAGE.delete
end

bootclasspath = [SDK_JAR, TRUFFLE_API_JAR]
classpath = oz_classpath

java = 'java'
java_opts = %w[-ea -esa]
argv = ARGV.dup
rest = []

while arg = argv.shift
  case arg
  when '-da'
    java_opts.delete('-ea')
    java_opts.delete('-esa')
  when '--help:graal'
    java_opts << "-Dgraal.PrintFlags=true"
    argv << "--graal"
  when '--graal'
    jvmci_home = (GRAAL / "mx.graal-core/env").read.scan(/^JAVA_HOME=(.+)/)[0][0]
    java = File.expand_path("#{jvmci_home}/bin/java")
    java_opts += %w[-server -XX:+UnlockExperimentalVMOptions -XX:+EnableJVMCI -d64]
    java_opts << "-Djvmci.class.path.append=#{GRAAL_JAR}"
    java_opts << "-Djvmci.Compiler=graal"
  when '--trace'
    java_opts << "-Dgraal.TraceTruffleCompilation=true"
  when '--fg'
    java_opts << "-Dgraal.TruffleBackgroundCompilation=false"
  when '--stress'
    java_opts << '-Dgraal.TruffleCompileImmediately=true'
    java_opts << '-Dgraal.TruffleBackgroundCompilation=false'
    java_opts << '-Dgraal.TruffleCompilationExceptionsAreFatal=true'
  when '--igv'
    java_opts << "-Dgraal.Dump=TruffleTree,PartialEscape,RemoveValueProxy"
    java_opts << "-Dgraal.PrintBackendCFG=false"
    java_opts << "-Dgraal.TruffleBackgroundCompilation=false"
  when '--igvcfg'
    java_opts << "-Dgraal.Dump="
    java_opts << "-Dgraal.PrintBackendCFG=true"
    java_opts << "-Dgraal.TruffleBackgroundCompilation=false"
  when '--infopoints'
    java_opts << "-XX:+UnlockDiagnosticVMOptions" << "-XX:+DebugNonSafepoints"
    java_opts << "-Dgraal.TruffleEnableInfopoints=true"
  when '--jdebug'
    java_opts << "-agentlib:jdwp=transport=dt_socket,server=y,address=51819,suspend=y"
  when /^-[DX](.+)/
    java_opts << arg
  else
    rest = [arg, *argv]
    break
  end
end

cmd = [
  java,
  *java_opts,
  "-Xbootclasspath/p:" + bootclasspath.join(':'),
  '-cp', classpath.join(':'),
  'org.mozartoz.truffle.OzLauncher'
] + rest

puts "$ #{cmd.join(' ')}"
exec(*cmd)
