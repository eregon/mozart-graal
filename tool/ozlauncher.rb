#!/usr/bin/env ruby

require_relative 'common'

dir = File.expand_path('..', __FILE__)

OZ_MAIN_SOURCES = Dir["#{PROJECT_DIR}/lib/main/**/*.oz"]
ALL_SOURCES = JAVA_SOURCES + SCALA_SOURCES + OZ_MAIN_SOURCES

PASSING_TESTS = %w[
  int.oz
  proc.oz
  dictionary.oz
  record.oz
  state.oz
  exception.oz
  float.oz
  conversion.oz
  type.oz
  byneed.oz
  future.oz
  tailrec.oz
  unification.oz
  onstack_clearing.oz
].map { |file| "platform-test/base/#{file}" }

if MAIN_IMAGE.exist? and mtime = MAIN_IMAGE.mtime and
    f = ALL_SOURCES.find { |src| File.mtime(src) > mtime }
  $stderr.puts "Removing Main.image because #{File.basename(f)} is more recent"
  MAIN_IMAGE.delete
end

classpath = oz_classpath

java = 'java'
java_opts = %w[-ea -esa]
java_opts << "-Doz.home=#{dir}"

argv = ARGV.dup
oz_options = []
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
    java = "#{OPENJDK_JVMCI_HOME}/bin/java"
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
  when /^--/
    oz_options << arg
  else
    rest = [arg, *argv]
    break
  end
end

if rest.empty?
  rest = ["platform-test/simple_runner.oz", *PASSING_TESTS]
end

cmd = [
  java,
  *java_opts,
  "-Xbootclasspath/p:" + oz_bootclasspath.join(':'),
  '-cp', classpath.join(':'),
  'org.mozartoz.truffle.OzLauncher'
] + oz_options + rest

puts "$ #{cmd.join(' ')}"
exec(*cmd)
