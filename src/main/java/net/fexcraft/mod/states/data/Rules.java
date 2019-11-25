package net.fexcraft.mod.states.data;

import java.util.ArrayList;
import java.util.Collection;

import net.fexcraft.mod.states.data.RuleSet.Initiator;
import net.fexcraft.mod.states.data.RuleSet.Rule;

public class Rules {
	
	public static Collection<Rule> MUNICIPIAL = getDefaultMunicipalityRules();

	private static Collection<Rule> getDefaultMunicipalityRules(){
		ArrayList<Rule> list = new ArrayList<>();
		list.add(new Rule("change.name", null, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		return list;
	}

}
