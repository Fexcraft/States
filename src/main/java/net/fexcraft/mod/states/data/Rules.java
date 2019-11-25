package net.fexcraft.mod.states.data;

import java.util.ArrayList;
import java.util.Collection;

import net.fexcraft.mod.states.data.root.Initiator;

public class Rules {
	
	public static Collection<Rule> MUNICIPIAL = getDefaultMunicipalityRules();

	private static Collection<Rule> getDefaultMunicipalityRules(){
		ArrayList<Rule> list = new ArrayList<>();
		list.add(new Rule("set.name", null, true, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		list.add(new Rule("set.price", null, true, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		list.add(new Rule("set.mayor", null, true, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		return list;
	}

}
