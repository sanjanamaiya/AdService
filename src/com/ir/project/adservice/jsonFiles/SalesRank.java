package com.ir.project.adservice.jsonFiles;

public class SalesRank 
{
	private String ToysGames;

    public String getToysAndGames()
    {
        return ToysGames;
    }

    public void setToysAndGames (String ToysGames)
    {
        this.ToysGames = ToysGames;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [Toys & Games = "+ToysGames+"]";
    }
}
