def rewrite(file)
  lines = File.readlines(file)
  lines = yield lines
  File.write(file, lines.join)
end

rewrite "vm/org.mozartoz.truffle.iml" do |lines|
  lines.map { |line|
    case line
    when /name="BOOTCOMPILER"/
      '<orderEntry type="module" module-name="bootcompiler" />' + "\n"
    else
      line
    end
  }
end

rewrite ".idea/modules.xml" do |lines|
  lines.map { |line|
    case line
    when /<modules>/
      line + <<-XML
      <module fileurl="file://$PROJECT_DIR$/bootcompiler/.idea/modules/bootcompiler.iml" filepath="$PROJECT_DIR$/bootcompiler/.idea/modules/bootcompiler.iml" />
      <module fileurl="file://$PROJECT_DIR$/bootcompiler/.idea/modules/bootcompiler-build.iml" filepath="$PROJECT_DIR$/bootcompiler/.idea/modules/bootcompiler-build.iml" />
      XML
    else
      line
    end
  }
end

# Unused
File.delete ".idea/libraries/BOOTCOMPILER.xml"
