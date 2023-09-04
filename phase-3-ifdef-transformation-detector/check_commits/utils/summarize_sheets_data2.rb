# gem install roo
require 'csv'

file_name = "analysis/phase2-analysis.csv"

CSV.foreach(file_name, headers: true) do |row|
  puts row.inspect
end

# Use the extension option if the extension is ambiguous.
# xlsx = Roo::Spreadsheet.open('./rails_temp_upload', extension: :xlsx)

# pp xlsx.info

# sheets_to_ignore = %w[status auxiliar Questions bfgminer]

# Count = ->(data, type) { data.count { |row| row[:type] == type } }
# CountTypeAndDirection = lambda { |data, type, direction|
#   data.count do |row|
#     row[:type] == type && row[:direction] == direction
#   end
# }
# CountAuthorsByDirection = lambda { |data, direction|
#   data.select { |row| row[:direction] == direction }.map { |row| row[:author] }.uniq.count
# }

# results = {}
# xlsx.sheets.each do |sheet_name|
#   next if sheets_to_ignore.include?(sheet_name)

#   sheet = xlsx.sheet(sheet_name)
#   parsed_rows = sheet.parse(type: 'type', direction: 'direction', add_disciplined: 'add disciplined',
#                             add_undisciplined: 'add undisciplined', author: 'author')

#   results[sheet_name] = {
#     d_to_nd: {
#       devs: CountAuthorsByDirection[parsed_rows, 'D -> ND'],
#       floss: CountTypeAndDirection[parsed_rows, 'Floss', 'D -> ND'],
#       root: CountTypeAndDirection[parsed_rows, 'Root', 'D -> ND']
#     },
#     nd_to_d: {
#       devs: CountAuthorsByDirection[parsed_rows, 'ND -> D'],
#       floss: CountTypeAndDirection[parsed_rows, 'Floss', 'ND -> D'],
#       root: CountTypeAndDirection[parsed_rows, 'Root', 'ND -> D']
#     }
#   }
# end

# # format to lattex
# results.each do |project, data|
#   occurencies_nd_to_d = data[:nd_to_d][:floss] + data[:nd_to_d][:root]
#   occurencies_d_to_nd = data[:d_to_nd][:floss] + data[:d_to_nd][:root]

#   puts "\\multicolumn{1}{|l}{#{project.capitalize}} & ? & ? & #{occurencies_nd_to_d} & #{data[:nd_to_d][:devs]} & #{data[:nd_to_d][:floss]} & #{data[:nd_to_d][:root]} & #{occurencies_d_to_nd} & #{data[:d_to_nd][:devs]} & #{data[:d_to_nd][:floss]} & #{data[:d_to_nd][:root]} \\\\"
# end

# sum_occurences_nd_to_d = results.map { |_, data| data[:nd_to_d][:floss] + data[:nd_to_d][:root] }.sum
# sum_devs_nd_to_d = results.map { |_, data| data[:nd_to_d][:devs] }.sum
# sum_floss_nd_to_d = results.map { |_, data| data[:nd_to_d][:floss] }.sum
# sum_root_nd_to_d = results.map { |_, data| data[:nd_to_d][:root] }.sum

# sum_occurences_d_to_nd = results.map { |_, data| data[:d_to_nd][:floss] + data[:d_to_nd][:root] }.sum
# sum_devs_d_to_nd = results.map { |_, data| data[:d_to_nd][:devs] }.sum
# sum_floss_d_to_nd = results.map { |_, data| data[:d_to_nd][:floss] }.sum
# sum_root_d_to_nd = results.map { |_, data| data[:d_to_nd][:root] }.sum
# puts "                                      & \\multicolumn{1}{l|}{}                & \\textbf{Total}                           &  #{sum_occurences_nd_to_d} & #{sum_devs_nd_to_d} & #{sum_floss_nd_to_d} & #{sum_root_nd_to_d} & #{sum_occurences_d_to_nd} & #{sum_devs_d_to_nd} & #{sum_floss_d_to_nd} & #{sum_root_d_to_nd} \\\\ \\cline{3-11} "
