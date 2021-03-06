require "fileutils"
require "nokogiri"

class String
  def underscore
    self.gsub(/::/, '/').
    gsub(/([A-Z]+)([A-Z][a-z])/,'\1_\2').
    gsub(/([a-z\d])([A-Z])/,'\1_\2').
    tr("-", "_").
    downcase
  end
end

TARGET_PATH = "betsy_tests"

FileUtils.rm_rf TARGET_PATH
FileUtils.mkdir TARGET_PATH

Dir.glob("rules/SA000*").each do |rule_path|

	rule_number = File.basename(rule_path).to_i
	rule_name = File.basename(rule_path)

	bpel_names = []

	Dir.glob("#{rule_path}/**/*.bpel").each do |bpel|

	   	bpel_filename = rule_name + "-" + File.basename(bpel)
	   	bpel_name = bpel_filename[0..-6]
		target_bpel_path = File.join(TARGET_PATH, "sa-rules", rule_name, bpel_name)
		target_bpel_file_path = File.join(target_bpel_path, bpel_filename)

		FileUtils.mkdir_p target_bpel_path
	   	FileUtils.cp bpel, target_bpel_file_path

		bpel_doc = Nokogiri::XML(File.open(target_bpel_file_path))

		# uniquefy name and targetNamespace
		bpel_process = bpel_doc.at_xpath("/bpel:process","bpel" => "http://docs.oasis-open.org/wsbpel/2.0/process/executable")
		bpel_process[:name] = bpel_name
		bpel_process[:targetNamespace] = "http://dsg.wiai.uniba.de/betsy/rules/#{rule_name.downcase}/bpel/#{bpel_name}"

		# correct import location and filenaming
		bpel_imports = bpel_doc.xpath("//bpel:import","bpel" => "http://docs.oasis-open.org/wsbpel/2.0/process/executable")
		bpel_imports.each do |import|
			location = import[:location]
			location_filename = location.gsub("../","")

			if(location_filename != "TestInterface.wsdl" and location_filename != "TestPartner.wsdl")
			   location_filename = "TestInterface.wsdl"
			end

			FileUtils.cp File.join(rule_path,location), File.join(target_bpel_path,location_filename)

			import[:location] = location_filename
		end

		# write new bpel file
		File.open(target_bpel_file_path, 'w') {|f| f.write(bpel_doc.to_xml) }

		# write groovy test case file
		wsdls = ["sa-rules/#{rule_name}/#{bpel_name}/TestInterface.wsdl"]
		wsdls << "sa-rules/#{rule_name}/#{bpel_name}/TestPartner.wsdl" if File.exists? "#{File.dirname(bpel)}/TestPartner.wsdl"
		File.open(File.join(TARGET_PATH,"tests.groovy"),"a") do |f|
		   f << <<-eos
		    public final Process #{bpel_name.underscore.upcase.gsub("-","_")} = new Process(
		        bpel: "sa-rules/#{rule_name}/#{bpel_name}/#{bpel_filename}",
                wsdls: [#{wsdls.map {|n| "\"#{n}\"" }.join ", "}],
                testCases: [new TestCase(testSteps: [new TestStep(input: "5", assertions: [new NotDeployableAssertion()], operation: WsdlOperation.SYNC)])]
            )

		   eos
		end
		bpel_names << bpel_name
	end

	File.open(File.join(TARGET_PATH,"tests.groovy"),"a") do |f|
	   f << <<-eos
	        public final List<Process> #{rule_name} = [
                #{bpel_names.map {|n| n.underscore.upcase.gsub("-","_") }.join ", " }
            ].flatten() as List<Process>

	   eos
	end

end

File.open(File.join(TARGET_PATH,"tests.groovy"),"a") do |f|
	   f << "public final List<Process> RULES = ["
       f << Dir.glob("rules/SA000*").map {|rule| File.basename(rule)}.join(", ")
       f << "].flatten() as List<Process>"
end
