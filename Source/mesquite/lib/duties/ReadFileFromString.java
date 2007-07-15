package mesquite.lib.duties;

import mesquite.lib.CommandRecord;
import mesquite.lib.Taxa;
import mesquite.lib.characters.CharacterData;

public interface ReadFileFromString {

	public void readFileFromString(CharacterData data, Taxa taxa, String contents, String arguments, CommandRecord commandRec);

		
}
