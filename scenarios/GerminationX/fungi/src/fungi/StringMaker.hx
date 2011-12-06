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
import truffle.RndGen;

class StringMaker
{
    var MsgMap:Dynamic;
    var NoteMap:Dynamic;

    static var Reasons =
        {{
            clover: "nutrients",
            dandelion: "nutrients",
            aronia: "erosion control",
            apple: "protection",
            cherry: "protection",
            boletus: "increased nutrients",
            chanterelle: "increased nutrients",
            flyagaric: "increased magic",
        }};

    public function new()
    {
        var Rnd = new RndGen();
        MsgMap={
            i_have_been_planted: function(from,to,owner,extra:Array<Dynamic>)
            {
                return owner+"'s "+from+" plant has just germinated!";
            },
            i_am_ill: function(from,to,owner,extra:Array<Dynamic>)
            {
                return owner+"'s "+from+" plant is feeling ill.";
            },
            i_have_fruited: function(from,to,owner,extra:Array<Dynamic>)
            {
                return owner+"'s "+from+" plant has fruited.";
            },
            i_have_died: function(from,to,owner,extra:Array<Dynamic>)
            {
                return owner+"'s "+from+" plant has died.";
            },
            i_have_recovered: function(from,to,owner,extra:Array<Dynamic>)
            {
                return owner+"'s "+from+" plant has recovered.";
            },
            i_have_been_picked_by: function(from,to,owner,extra:Array<Dynamic>)
            {
                return owner+"'s "+from+" plant has been picked by "+extra[0];
            },
            your_plant_doesnt_like: function(from,to,owner,extra:Array<Dynamic>)
            {
                return extra[0]+", your "+extra[3]+" plant doesn't like "+to+"'s "+extra[2]+" plant nearby.";
            },
            your_plant_needs: function(from,to,owner,extra:Array<Dynamic>)
            {
                return to+", your "+extra[0]+" plant needs a "+extra[1]+" plant nearby for "+Reflect.field(Reasons,extra[1])+".";
            },
            needs_help: function(from,to,owner,extra:Array<Dynamic>)
            {
                return to+", "+extra[0]+"'s "+extra[2]+" plant needs a "+extra[3]+" near.";
            },
            i_am_recovering: function(from,to,owner,extra:Array<Dynamic>)
            {
                return owner+"'s "+from+" plant is recovering.";
            },
            i_am_detrimented_by: function(from,to,owner,extra:Array<Dynamic>)
            {
                return owner+"'s "+from+" plant is being harmed by "+extra[0]+"'s "+extra[2]+" plant.";
            },
            i_am_detrimental_to: function(from,to,owner,extra:Array<Dynamic>)
            {
                return owner+"'s "+from+" plant is harming "+extra[0]+"'s "+extra[2]+" plant.";
            },
            i_am_benefitting_from: function(from,to,owner,extra:Array<Dynamic>)
            {
                return owner+"'s "+from+" plant is being helped by "+extra[0]+"'s "+extra[2]+" plant.";
            },
            i_am_beneficial_to: function(from,to,owner,extra:Array<Dynamic>)
            {
                return owner+"'s "+from+" plant is helping "+extra[0]+"'s "+extra[2]+" plant.";
            },
            thanks_for_helping: function(from,to,owner,extra:Array<Dynamic>)
            {
                return owner+"'s "+from+" plant thanks "+to+"'s "+extra[0]+" plant for helping.";
            },
            spirit_helper_praise: function(from,to,owner,extra:Array<Dynamic>)
            {
                return "I'm happy "+to+"'s "+extra[0]+" plant is providing my plants with "+
                    Reflect.field(Reasons,extra[0])+".";
            },
            spirit_growing_praise: function(from,to,owner,extra:Array<Dynamic>)
            {
                return "I'm happy "+to+"'s "+extra[0]+" plant is growing well.";
            },
            spirit_flowering_praise: function(from,to,owner,extra:Array<Dynamic>)
            {
                return "It makes me joyful to see "+to+"'s "+extra[0]+" is flowering!";
            },
            spirit_fruiting_praise: function(from,to,owner,extra:Array<Dynamic>)
            {
                return "This "+extra[0]+" plant, sewn by "+to+" I'm glad to announce, has fruited";
            },
            spirit_general_praise: function(from,to,owner,extra:Array<Dynamic>)
            {
                return to+"'s "+Rnd.Choose(["wonderful","lush","beautiful"])+" "+extra[0]+
                    " plant is one of my favorite kinds of plant.";
            },
            ive_asked_x_for_help: function(from,to,owner,extra:Array<Dynamic>)
            {
                var msg=to+", I've asked "+extra[0]+" to help with your "+extra[2]+" plant";
                if (extra[3]=="ill-a") msg+=", which is a little ill.";
                else if (extra[3]=="ill-b") msg+=", which is a quite ill.";
                else if (extra[3]=="ill-c") msg+=", which is a very ill.";
                else msg+=".";
                return msg;
            },
            one_time_i_have_flowered: function(from,to,owner,extra:Array<Dynamic>)
            {
                return to+", your "+from+" plant has flowered for the first time, and your score has increased!";
            },
            gift_received: function(from,to,owner,extra:Array<Dynamic>)
            {
                return extra[0]+", you have been given a "+extra[2]+" fruit by "+extra[1]+"!";
            },
            gift_sent: function(from,to,owner,extra:Array<Dynamic>)
            {
                return extra[0]+", you have given a "+extra[2]+" fruit to "+extra[1]+"!";
            }
        };

        NoteMap={
            welcome: function(name)
            {
                return 
                "Welcome to Germination X "+name+". \n"+ 
                    "<img src=\"images/welcome.png\"> ";
            },

            levelup1: function (name)
            {
                return "Well done on planting all those ground cover plants "+name+"!\n"+
                    "<img src=\"images/levelup1.png\"> ";

//                return "Well done on planting all those ground cover plants "+name+"!\n"+
//                    "You will find a mystery magic item in your fruit store - use it wisely! Your new role is to plant shrub plants with fruit like this:"+
//                    "<img src=\"images/aronia-fruit-c.png\">\n"+
//                    "The shrub spirit will help you in this quest. You can also send fruit to other players or spirits by dropping them on messages.";
            },

            levelup2: function (name)
            {
                return name+", you are a master gardener!\n"+
                    "<img src=\"images/levelup2.png\"> ";

//                return name+", you are a master gardener!\n"+
//                    "Your next role in Germination X is to plant tree plants with fruit like this:"+
//                    "<img src=\"images/cherry-fruit-c.png\"> <img src=\"images/apple-fruit-c.png\">\n"+
//                    "You should also find another surprise magic item!";
            },

            levelup3: function (name)
            {
                return name+", you have proved your planting prowess, and are now an expert on companion planting and permaculture!\n"+
                    "\nCongratulations, you have completed the current version of Germination X."+
                    "\n\nYou have earned a final surprise as a gift, from now on you can pick and plant any type of plant."+
                    "\n\nThankyou for playing!";                
            }
        };
    }

    public function MsgToString(msg:Dynamic) : String
    {
        if (!Reflect.hasField(MsgMap,msg.code))
        {
            return "oops - no message found for "+msg.code;
        }
        else
        {
            var owner = msg.owner;
            if (msg.type=="spirit") owner=msg.from;
            return MsgMap[msg.code]
            (  
                msg.from,
                msg.display, 
                owner,
                msg.extra
            );
        }
    }

    public function NoteToString(name:String,note:Dynamic) : String
    {
        if (!Reflect.hasField(NoteMap,note.code))
        {
            return "oops - no note found for "+note.code;
        }
        else
        {
            return NoteMap[note.code]
            (  
                name
            );
        }
    }
}