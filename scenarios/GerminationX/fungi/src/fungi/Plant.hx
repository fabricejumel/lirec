// GerminationX Copyright (C) 2010 FoAM vzw    \_\ __     /\
//                                          /\    /_/    / /  
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as
// published by the Free Software Foundation, either version 3 of the
// License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package fungi;

import truffle.Truffle;
import truffle.Vec2;
import truffle.Vec3;
import truffle.SpriteEntity;
import truffle.RndGen;

class Plant extends SpriteEntity 
{
    public var Id:Int;
	public var Owner:Int;
	var PlantScale:Float;
	public var Age:Int;
    var Scale:Float;
    var PlantType:String;
    public var State:String;
    var Seeds:Array<Seed>;
    var Layer:String;
    var Star:Sprite;
    var Owned:Bool;
    var Rnd:RndGen;
    var ServerPos:Vec2;
    var ServerTile:Vec2;

    static var CentrePositions = {
        { 
            clover: new Vec2(0,-50),
            dandelion: new Vec2(0,-200),
            aronia: new Vec2(0,-120),
            apple: new Vec2(0,-140),
            cherry: new Vec2(0,-210),
            boletus: new Vec2(0,-50),
            chanterelle: new Vec2(0,-50),
            flyagaric: new Vec2(0,-50)
        }};

    // because not all states are represented by graphics
    function FixState(state:String): String
    {
        if (state=="planted") return "grow-a";
        if (state=="fruit-a") return "grown";
        if (state=="fruit-b") return "grown";
        if (state=="fruit-c") return "grown";
        return state;
    }

	public function new(world:World, plant, pos, servertile)
	{
        State=FixState(plant.state);
        PlantType=plant.type;
		super(world,pos,Resources.Get(PlantType+"-"+State),false);
        Id=Std.parseInt(plant.id);
		Owner=Std.parseInt(Reflect.field(plant,"owner-id"));
        PlantScale=0;
        //NeedsUpdate=true;
        Seeds=[];
        Layer=plant.layer;
        Owned=false;
        Spr.Hide(false);
        Rnd=new RndGen();
        Rnd.Seed(Id);
        ServerTile=new Vec2(servertile.x,servertile.y);
        ServerPos=new Vec2(plant.pos.x,plant.pos.y);

        for (i in 0...Std.parseInt(plant.fruit)) 
        {
            Fruit(world);
        }

        // display stars next to plants owned by the player
        if (!Owned && Owner==world.MyID)
        {
            Star = new Sprite(Reflect.field(CentrePositions,PlantType),
                              Resources.Get("star"));
            world.AddSprite(Star);
            Owned=true;
            Star.Update(0,Spr.Transform);
        }
	}

    public function StateUpdate(world:World,plant)
    {
        var s=FixState(plant.state);
        // if the state has changed
        if (State!=s)
        {
            State=s;

            if (State!="decayed")
            {
                Spr.ChangeBitmap(Resources.Get(PlantType+"-"+State));
            }

            if (State=="fruit-a" ||
                State=="fruit-b" ||
                State=="fruit-c")
            {
                for (s in Seeds)
                {
                    s.ChangeState(State);
                }
            };
            // display stars next to plants owned by the player
            if (!Owned && Owner==world.MyID)
            {
                Star = new Sprite(Reflect.field(CentrePositions,PlantType),
                                  Resources.Get("star"));
                world.AddSprite(Star);
                Owned=true;
                Star.Update(0,Spr.Transform);
            }
        }

        // see if any seeds have been picked or arrived
        var FruitDiff = Std.parseInt(plant.fruit)-Seeds.length;
        if (FruitDiff!=0)
        {
            if (FruitDiff>0)
            {
                for (i in 0...FruitDiff) Fruit(world);
            }
            else
            {
                for (i in 0...-FruitDiff) Unfruit(world);
            }
        }
    }

    override function Destroy(world:World)
    {
        super.Destroy(world);
        for (seed in Seeds)
        {
            world.RemoveSprite(seed.Spr);
        }
        if (Owned) world.RemoveSprite(Star);
    }
	
	public override function Update(frame:Int, world:World)
	{
		super.Update(frame,world);
        for (seed in Seeds)
        {
            seed.Spr.Update(frame,Spr.Transform);
        }
        
        if (Owned) Star.Update(frame,Spr.Transform);
	}

    override function OnSortScene(world:World, order:Int) : Int
    {
        Spr.SetDepth(order++);
        for (seed in Seeds)
        {
            world.setChildIndex(seed.Spr,order++);
        }        
        if (Owned) Star.SetDepth(order++);
        return order;
    }

    public function Fruit(world:World)
    {
        var Pos:Vec2=Reflect.field(CentrePositions,PlantType)
            .Add(Rnd.RndCircleVec2().Mul(32));
        var Fruit=new Seed(Pos,PlantType,0);
        world.AddSprite(Fruit.Spr);
        Seeds.push(Fruit);
        Update(0,world);
        Fruit.Spr.MouseDown(this,function(p) 
        {            
            if (world.MyName!="")// && f.State=="fruit-c")
            {
                // arsing around with the sprites to get
                // better feedback for the player
                p.Seeds.remove(Fruit);
                world.RemoveSprite(Fruit.Spr);

                // get the server tile
                var ServerTileWidth:Int=5;
                var TilePosX:Int = cast(world.WorldPos.x,Int)+
                    Math.floor(p.LogicalPos.x/ServerTileWidth)-1;
                var TilePosY:Int = cast(world.WorldPos.y,Int)+
                    Math.floor(p.LogicalPos.y/ServerTileWidth)-1;

                world.Server.Request(
                    "pick/"+
                        Std.string(TilePosX)+"/"+
                        Std.string(TilePosY)+"/"+
                        Std.string(p.Id)+"/"+
                        Std.string(world.MyID),
                    world,
                    function (c,d) 
                    {
                        if (d.ok==true)
                        {
                            /*// make a brand new one
                            var ns = new Seed(Fruit.Spr.Pos,Fruit.Type);
                            ns.ChangeState("fruit-c");
                            c.Seeds.Add(cast(c,World),ns);
                            c.AddSprite(ns.Spr);
                            c.SortScene();*/
                        }
                    });
            }
        });
    }

    function Unfruit(world:World)
    {
        if (Seeds.length>0)
        {
            var seed:Seed=Rnd.Choose(Seeds);
            world.RemoveSprite(seed.Spr);
            Seeds.remove(seed);        
        }
    }
}
