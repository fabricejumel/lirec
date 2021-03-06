// t r u f f l e Copyright (C) 2010 FoAM vzw   \_\ __     /\
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

package truffle.flash;

import flash.display.Sprite;
import flash.display.Bitmap;
import flash.display.BitmapData;
import flash.events.MouseEvent;
import flash.events.Event;
import flash.events.IOErrorEvent;
import flash.geom.Matrix;
import flash.geom.Point;
import flash.net.URLRequest;
import flash.display.Loader;
import flash.geom.ColorTransform;
import truffle.Vec2;
import truffle.Vec3;
import truffle.interfaces.Sprite;
import truffle.interfaces.World;
import truffle.interfaces.TextureDesc;

class FlashSprite implements truffle.interfaces.Sprite, extends flash.display.Sprite
{	
    public var Pos:Vec2;
    public var Width:Int;
    public var Height:Int;
    public var Angle:Float;
    public var MyScale:Vec2;
    public var Hidden:Bool;
    public var Centre:Vec2;
    var PreTransformDirty:Bool;
    public var PreTransform:Matrix;
    public var Transform:Matrix;
    public var Colour:Vec3;
    public var OffsetColour:Vec3;
    var PostTransform:Matrix;
    var Depth:Int;
    var MouseDownFunc:Dynamic -> Void;
    var MouseDownContext:Dynamic;
    var MouseUpFunc:Dynamic -> Void;
    var MouseUpContext:Dynamic;
    var MouseOverFunc:Dynamic -> Void;
    var MouseOverContext:Dynamic;
    var MouseOutFunc:Dynamic -> Void;
    var MouseOutContext:Dynamic;
    var DoCentreMiddleBottom:Bool;

	public function new(pos:Vec2, t:TextureDesc, midbot:Bool=false, viz=true) 
	{
		super();
        visible=viz;
        Pos=pos;
        Angle=0;
        Depth=-1;
        MyScale = new Vec2(1,1);
        PreTransformDirty=true;
        PreTransform = new Matrix();
        Transform = new Matrix();
        PostTransform = new Matrix();
        Width=64;
        Height=112;
        Centre=new Vec2(0,0);
        DoCentreMiddleBottom=midbot;
        ChangeBitmap(t);
        EnableMouse(false);
        Colour=null;
        OffsetColour=null;
        Update(0,null); // <-- calls inherited class update???

        cacheAsBitmap=true; // optimisation!!! - turn off if rotating
	}

    public function Hide(s:Bool) : Void
    {
        Hidden=s;
        visible=!s;
    }

	public function MouseDown(c:Dynamic, f:Dynamic -> Void=null)
	{
        EnableMouse(true);
        MouseDownFunc=f;
        MouseDownContext=c;
		addEventListener(MouseEvent.MOUSE_DOWN, MouseDownCB);
	}

    public function MouseDownCB(e)
    {
        MouseDownFunc(MouseDownContext);
    }

	public function MouseUp(c:Dynamic, f:Dynamic -> Void=null)
	{
        EnableMouse(true);
        MouseUpFunc=f;
        MouseUpContext=c;
		addEventListener(MouseEvent.MOUSE_UP, MouseUpCB);
	}

    public function MouseUpCB(e)
    {
        MouseUpFunc(MouseUpContext);
    }

	public function MouseOver(c:Dynamic, f:Dynamic -> Void=null)
	{
        EnableMouse(true);
        MouseOverFunc=f;
        MouseOverContext=c;
		addEventListener(MouseEvent.MOUSE_OVER, MouseOverCB);
	}

    public function MouseOverCB(e)
    {
        MouseOverFunc(MouseOverContext);
    }

	public function MouseOut(c:Dynamic, f:Dynamic -> Void=null)
	{
        EnableMouse(true);
        MouseOutFunc=f;
        MouseOutContext=c;
		addEventListener(MouseEvent.MOUSE_OUT, MouseOutCB);
	}

    public function MouseOutCB(e)
    {
        MouseOutFunc(MouseOutContext);
    }

    public function SetDepth(s:Int)
    {
        parent.setChildIndex(this,s);
        Depth=s;
    }
    
    public function GetDepth() : Int
    {
        return Depth;
    }

    public function CentreMiddleBottom(s:Bool) : Void
    {
        DoCentreMiddleBottom=s;
    }

    function SetSize(x:Int,y:Int)
    {
        Width=x;
        Height=y;
        if (DoCentreMiddleBottom)
        {
            Centre.x=Width/2;
            Centre.y=Height;            
        }
        else
        {
            Centre.x=Width/2;
            Centre.y=Height/2;
        }
    }

    public function EnableMouse(s:Bool)
    {
        mouseEnabled=s;
        mouseChildren=s;
    }

    public function IsMouseEnabled()
    {
        return mouseEnabled;
    }

	public function ChangeBitmap(t:TextureDesc)
	{
        var tex = cast(t,truffle.flash.FlashTextureDesc);
        SetSize(tex.data.width,tex.data.height);
		graphics.clear();
		graphics.beginBitmapFill(tex.data);
        graphics.drawRect(0,0,Width,Height);
		graphics.endFill();
	}

    public function LoadFromURL(url:String)
    {
        var loader:Loader = new Loader();
        loader.contentLoaderInfo.addEventListener(IOErrorEvent.IO_ERROR, 
                                                  function(e:IOErrorEvent):Void 
                                                  {  
                                                      trace(e.text+' '+url);
                                                  });

        loader.contentLoaderInfo.addEventListener(Event.COMPLETE, ImageLoaded);
        loader.load(new URLRequest(url)); 
    }
    
    function ImageLoaded(e:Event)
    {        
        e.target.content.smoothing = true;
        var dupBitmap:Bitmap = new Bitmap(cast(e.target.content,Bitmap).bitmapData);
        SetSize(cast(dupBitmap.width,Int),cast(dupBitmap.height,Int));
		graphics.clear();
		graphics.beginBitmapFill(dupBitmap.bitmapData);
        graphics.drawRect(0,0,Width,Height);
		graphics.endFill();   
    }

    public function SetPos(s:Vec2) { Pos=s; PreTransformDirty=true; }
	public function SetScale(s:Vec2) { MyScale=s; PreTransformDirty=true; }
	public function SetRotate(angle:Float) { Angle=angle; PreTransformDirty=true; }
    public function GetTx() : Dynamic { return Transform; }

    public function TransformedPos() : Vec2
    {
        var p:Point = new Point(0,0);
        p=Transform.transformPoint(p);
        return new Vec2(p.x,p.y);
    }

	public function Update(frame:Int, tx:Dynamic)
	{
        if (PreTransformDirty==true)
        {
            PreTransform.identity();        
		    PreTransform.rotate(Angle*(Math.PI/180));
            PreTransform.scale(MyScale.x,MyScale.y);
            PreTransform.translate(Pos.x,Pos.y);
            PreTransformDirty=false;
        }

        Transform=PreTransform.clone();
        if (tx!=null)
		{
            Transform.concat(tx);
        }

        PostTransform.identity();
        // we don't want to pass on the centering offset to the hierachy
        PostTransform.translate(-Centre.x, -Centre.y);
        PostTransform.concat(Transform);
        transform.matrix=PostTransform;

        if (Colour!=null)
        {
            transform.colorTransform = new ColorTransform(Colour.x,Colour.y,Colour.z,1,0,0,0,0);
	    }

        if (OffsetColour!=null)
        {
            transform.colorTransform = new ColorTransform(1,1,1,1,OffsetColour.x,OffsetColour.y,OffsetColour.z,0);
	    }
    }
}
