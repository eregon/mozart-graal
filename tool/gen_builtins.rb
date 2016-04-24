require 'json'

file = ARGV[0] || 'builtins/ModThread-builtin.json'
mod = JSON.parse(File.read(file), symbolize_names: true)

mod_name = mod[:name]

mod[:builtins].each { |builtin|
  builtin_name = builtin[:name]
  fun_name = builtin_name
  fun_name += mod_name if %w[is new create this].include?(builtin_name)
  class_name = fun_name[0].upcase + fun_name[1..-1]
  params = builtin[:params]
  is_proc = params.empty? || params.last[:kind] != "Out"
  inputs = is_proc ? params : params[0...-1]
  children = if inputs.empty?
    ""
  elsif inputs.size == 1
    "\t@NodeChild(\"#{inputs[0][:name]}\")"
  else
    "\t@NodeChildren({ " + inputs.map { |par|
      "@NodeChild(\"#{par[:name]}\")"
    } * ", " + " })"
  end
  fun_args = inputs.map { |par|
    "#{par[:kind] == 'Out' ? 'OzVar' : 'Object'} #{par[:name]}"
  } * ', '

  changed_name = fun_name != builtin_name
  if is_proc && changed_name
    puts "\t@Builtin(name = \"#{builtin_name}\", proc = true)"
  elsif is_proc
    puts "\t@Builtin(proc = true)"
  elsif changed_name
    puts "\t@Builtin(name = \"#{builtin_name}\")"
  end
  puts <<EOJ
\t@GenerateNodeFactory
#{children}
\tpublic static abstract class #{class_name}Node extends OzNode {

\t\t@Specialization
\t\tObject #{fun_name}(#{fun_args}) {
\t\t\treturn unimplemented();
\t\t}

\t}
EOJ
  puts
}
