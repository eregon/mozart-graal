#!/usr/bin/env ruby

require_relative 'tool/common'

dir = File.expand_path('..', __FILE__)

bootclasspath = [TRUFFLE_API_JAR]
classpath = oz_classpath

java = 'java'
java_opts = %w[-ea -esa]

if ARGV.delete('--graal')
  jvmci_home = File.read("../graal-core/mx.graal-core/env").scan(/^JAVA_HOME=(.+)/)[0][0]
  java = File.expand_path("#{jvmci_home}/bin/java")
  java_opts += %w[-server -XX:+UnlockExperimentalVMOptions -XX:+EnableJVMCI -d64]
  java_opts << "-Djvmci.class.path.append=#{GRAAL_JAR}"
  java_opts << "-Djvmci.Compiler=graal"
end

args = ARGV.drop_while { |arg|
  if arg.start_with? '-'
    java_opts << arg
  end
}

cmd = [
  java,
  *java_opts,
  "-Xbootclasspath/p:" + bootclasspath.join(':'),
  '-cp', classpath.join(':'),
  'org.mozartoz.truffle.Main'
] + args

puts "$ #{cmd.join(' ')}"
exec(*cmd)
