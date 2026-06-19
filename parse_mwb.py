import xml.etree.ElementTree as ET
import sys

def parse_mwb_xml(xml_file):
    try:
        tree = ET.parse(xml_file)
        root = tree.getroot()
        
        tables = []
        
        # In MWB XML, objects are usually defined with <value type="object" struct-name="db.mysql.Table" id="...">
        for elem in root.iter('value'):
            if elem.get('type') == 'object' and elem.get('struct-name') == 'db.mysql.Table':
                table_name = ""
                columns = []
                
                for child in elem:
                    if child.tag == 'value' and child.get('key') == 'name':
                        table_name = child.text
                    elif child.tag == 'value' and child.get('key') == 'columns':
                        for col_elem in child:
                            if col_elem.get('type') == 'object' and col_elem.get('struct-name') == 'db.mysql.Column':
                                col_name = ""
                                col_type = ""
                                for col_child in col_elem:
                                    if col_child.tag == 'value' and col_child.get('key') == 'name':
                                        col_name = col_child.text
                                    elif col_child.tag == 'link' and col_child.get('key') == 'simpleType':
                                        col_type = col_child.text
                                if col_name:
                                    columns.append(f"  - {col_name} ({col_type if col_type else 'unknown'})")
                
                if table_name:
                    tables.append({"name": table_name, "columns": columns})
        
        print(f"Found {len(tables)} tables:")
        for t in tables:
            print(f"Table: {t['name']}")
            for c in t['columns']:
                print(c)
            print("")
            
    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    if len(sys.argv) > 1:
        parse_mwb_xml(sys.argv[1])
    else:
        print("Provide path to document.mwb.xml")
