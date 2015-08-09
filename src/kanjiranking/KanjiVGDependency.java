package kanjiranking;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import playground.RangeTrieMap;

public class KanjiVGDependency {
	
	public static class Kanji {
		String c;
		String id;
		
		HashSet<Kanji> components;
		
		@Override
		public int hashCode() {
			return c.hashCode();
		}
		
		@Override
		public boolean equals(Object o) {
			if(!(o instanceof Kanji)) return false;
			Kanji ko = (Kanji) o;
			boolean result = c == ko.c || c.equals(ko.c);
			return result;
		}
		
		public String toString() {
			return c; // + "(" + id + ")";
		}
		
		public String toStringTest() {
			StringBuilder sb = new StringBuilder();
			toStringTest(sb, new ArrayList<Kanji>());
			return sb.toString();
		}
		
		public void toStringTest(StringBuilder sb, ArrayList<Kanji> path) {
			if(path != null && path.contains(this)) {
				StringBuilder err = new StringBuilder();
				/*if(components != null) {
					for(Kanji k : components) {
						err.append(k.toString());
						err.append(", ");
					}
				}
				System.out.println("error: cyclic path " + err.toString());*/
				return;
			}
			if(path.size() > 100) {
				StringBuilder err = new StringBuilder();
				if(components != null) {
					for(Kanji k : components) {
						err.append(k.toString());
						err.append(", ");
					}
				}
				System.out.println("path too long! " + err.toString());
				return;
			}
			path.add(this);
			sb.append(toString());
			
			if(components != null) {
				sb.append(" [");
				for(Kanji k : components) {
					k.toStringTest(sb, path);
				}
				sb.append("]");
			}
		}
	}
	
	public static <T> T getLastNonNull(ArrayList<T> list) {
		for(int i=list.size()-1; i>=0; i--) {
			if(list.get(i) != null) return list.get(i);
		}
		return null;
	}
	
	public static void main(String... args) {
		String kanjivgFile = "/home/tr/Studium/sonstiges/languages/jap/kanjivg/kanjivg-20150615-2.xml";
		
		RangeTrieMap<Kanji> mrm = new RangeTrieMap<>();
		
		XMLInputFactory xif = XMLInputFactory.newFactory();
		xif.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);
		InputStream is;
		try {
			is = Files.newInputStream(Paths.get(kanjivgFile));
			XMLStreamReader xsr = xif.createXMLStreamReader(is);
			ArrayList<String> tagstack = new ArrayList<String>();
			ArrayList<Kanji> kanjicomponentstack = new ArrayList<Kanji>();
			
			int it=0;
			
			while(xsr.hasNext()) {
				//if(it++ > 150000) break;
				int event = xsr.next();
				switch(event) {
				case XMLStreamConstants.START_ELEMENT:
					Kanji kanjicomponent = null;
					String id = null;
					for(int i=0; i<xsr.getAttributeCount(); i++) {
						String attribName = xsr.getAttributeName(i).toString();
						String attribValue = xsr.getAttributeValue(i).trim();
						
						if("element".equals(attribName)) {
							Kanji parent = getLastNonNull(kanjicomponentstack);
							if(parent == null) {
								kanjicomponent = mrm.computeIfAbsent(attribValue, key -> {
									Kanji k = new Kanji();
									k.c = attribValue;
									return k;
								});
							}
							else {
								kanjicomponent = new Kanji();
								kanjicomponent.c = attribValue;
								if(parent != kanjicomponent) {
									if(parent.components == null) {
										parent.components = new HashSet<>();
									}
									parent.components.add(kanjicomponent);
								}
							}
						}
						else if("id".equals(attribName)) {
							id = attribValue;
						}
						
					}
					tagstack.add(xsr.getLocalName());
					if(kanjicomponent != null) {
						kanjicomponent.id = id;
					}
					kanjicomponentstack.add(kanjicomponent);

					//System.out.println(tagstack + " " + xsr.getAttributeCount());
					break;
				case XMLStreamConstants.ATTRIBUTE:
					System.out.println(xsr.getAttributeName(xsr.getAttributeCount()));
					break;
				case XMLStreamConstants.END_ELEMENT:
					tagstack.remove(tagstack.size()-1);
					kanjicomponentstack.remove(kanjicomponentstack.size()-1);
					break;
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
		
		int j=0;
		for(Entry<CharSequence, Kanji> k : mrm.entrySet()) {
			System.out.println(k.getValue().toStringTest());
			/*if(j == 12) {
				j = 12;
			}*/
			//if(j++ > 22) break;
		}
	}
}
