# frozen_string_literal: true

require 'csv'

module Report
  def read_files
    File.open('out.csv', 'w') do |output_file|
      output_file.write("report,url,old_path,new_path,old_loc,new_loc,old_block,new_block,old_disciplined,new_disciplined,old_undisciplined,new_undisciplined,commit_hash,author\n")
      urls = []
      Dir.glob('../output/*.csv').each do |file_path|
        report = file_path.gsub('../output/', '').gsub('_report.csv', '')
        CSV.foreach(file_path, headers: true).with_index do |line, _line_number|
          next if line['url'].empty?

          output_file.write("#{report},#{line['url']}/commit/#{line['commit_hash']}?split=diff,#{line[1..].join(',')}\n")
        end
      end
    end
  end

  module_function :read_files
end

Report.read_files
