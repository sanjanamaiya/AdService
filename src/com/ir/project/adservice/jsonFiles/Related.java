package com.ir.project.adservice.jsonFiles;

public class Related 
{
	private String[] bought_together;

    private String[] also_viewed;

    private String[] buy_after_viewing;
    
    private String[] also_bought;

    public String[] getBought_together ()
    {
        return bought_together;
    }

    public void setBought_together (String[] bought_together)
    {
        this.bought_together = bought_together;
    }

    public String[] getAlso_viewed ()
    {
        return also_viewed;
    }

    public void setAlso_viewed (String[] also_viewed)
    {
        this.also_viewed = also_viewed;
    }

    public String[] getAlso_bought ()
    {
        return also_bought;
    }

    public void setAlso_bought (String[] also_bought)
    {
        this.also_bought = also_bought;
    }
    
    public String[] getBuy_after_viewing ()
    {
        return buy_after_viewing;
    }

    public void setBuy_after_viewing (String[] buy_after_viewing)
    {
        this.buy_after_viewing = buy_after_viewing;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [bought_together = "+bought_together+", also_viewed = "+also_viewed+", also_bought = "+also_bought+"]";
    }

}
