package net.fexcraft.mod.states.api.root;

public interface Taxable {

	public long lastTaxCollection();
	
	public void onTaxCollected(long time);
	
	/** Leave as 0 to disable **/
	public long getCustomTax();
	
	public void setCustomTax(long newtax);
	
}
