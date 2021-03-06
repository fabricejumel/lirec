//----------------------------------------------------------------------
package eu.lirec.pleo;
//----------------------------------------------------------------------                

import java.util.Locale;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.location.Location;
import android.location.LocationListener;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
//----------------------------------------------------------------------                
class S3DSurfaceView extends GLSurfaceView implements SensorEventListener, LocationListener //, OnClickListener //, OnKeyboardActionListener
{
    //------------------------------------------------------------------
    // Constants.
    //
    private static final int kDeviceModelUnknown            =  0 ;
    private static final int kDeviceModelMotorolaDroid      =  1 ; // Same than Motorola Milestone
    private static final int kDeviceModelGoogleNexusOne     =  2 ;
    private static final int kDeviceModelSamsungCorby       =  3 ;
    private static final int kDeviceModelSamsungGalaxySpica =  4 ;
    private static final int kDeviceModelSamsungGalaxyS     =  5 ;
	private static final int kDeviceModelSamsungGalaxyTab	=  6 ;
    private static final int kDeviceModelSamsungGalaxy      =  7 ;
    private static final int kDeviceModelSamsungWave        =  8 ;
    @SuppressWarnings("unused")
	private static final int kDeviceModelSamsungBorby       =  9 ;
    private static final int kDeviceModelSamsungJet         = 10 ;
    private static final int kDeviceModelNVidiaTegra250     = 11 ;
    private static final int kDeviceModelHTCHero            = 12 ;
    private static final int kDeviceModelHTCBravo           = 13 ; // Same than HTC Desire
    private static final int kDeviceModelHTCMagic           = 14 ; // Same board than HTC Dream
    private static final int kDeviceModelHTCAria            = 15 ;
    private static final int kDeviceModelHTCTatoo           = 16 ;
    private static final int kDeviceModelHTCLegend          = 17 ;
    private static final int kDeviceModelHTCDroidIncredible = 18 ;
    private static final int kDeviceModelHTCDroidEris       = 19 ;
    private static final int kDeviceModelHTCEvo4G           = 20 ;
    
    
    //------------------------------------------------------------------
    // Constructor.
    //
    public S3DSurfaceView ( Context context, String cacheDirPath, String homeDirPath, String packDirPath, boolean wantGLES2, boolean forceDefaultOrientation )
    {
        super ( context ) ;
        
        _context = context;
        
        // Required to get key events
        //
        setFocusableInTouchMode ( true ) ;
        
        // Remember useful directories
        //
        sCacheDirPath   = cacheDirPath ;
        sHomeDirPath    = homeDirPath  ;
        sPackDirPath    = packDirPath  ;
        
        // Create the renderer
        //
        oRenderer       = new S3DRenderer ( context, sCacheDirPath, sHomeDirPath, sPackDirPath, forceDefaultOrientation ) ;
        
        // Detect device model
        //
        detectDeviceModel ( ) ;
        
        // Initialize OpenGL ES
        //
        initOpenGLES    ( wantGLES2 ) ;
        
        // Finally set the renderer
        //
        setRenderer     ( oRenderer ) ;
    }
    
    //------------------------------------------------------------------
    // Device model detection.
    //        
    private void detectDeviceModel ( )
    {
        if      ( Build.BOARD.contains ( "GT-I5500"    ) ) iDeviceModel = kDeviceModelSamsungCorby       ;
        else if ( Build.BOARD.contains ( "GT-I5700"    ) ) iDeviceModel = kDeviceModelSamsungGalaxySpica ;
        else if ( Build.BOARD.contains ( "GT-I7500"    ) ) iDeviceModel = kDeviceModelSamsungGalaxy      ;
        else if ( Build.BOARD.contains ( "GT-S8000"    ) ) iDeviceModel = kDeviceModelSamsungJet         ;
        else if ( Build.BOARD.contains ( "GT-S8500"    ) ) iDeviceModel = kDeviceModelSamsungWave        ;
        else if ( Build.BOARD.contains ( "GT-I9000"    ) ) iDeviceModel = kDeviceModelSamsungGalaxyS     ;
		else if ( Build.BOARD.contains ( "GT-P1000"	   ) ) iDeviceModel = kDeviceModelSamsungGalaxyTab   ;
        else if ( Build.BOARD.contains ( "sholes"      ) ) iDeviceModel = kDeviceModelMotorolaDroid      ;
        else if ( Build.BOARD.contains ( "mahimahi"    ) ) iDeviceModel = kDeviceModelGoogleNexusOne     ;
        else if ( Build.BOARD.contains ( "harmony"     ) ) iDeviceModel = kDeviceModelNVidiaTegra250     ;
        else if ( Build.BOARD.contains ( "heroc"       ) ) iDeviceModel = kDeviceModelHTCHero            ;
        else if ( Build.BOARD.contains ( "bravo"       ) ) iDeviceModel = kDeviceModelHTCBravo           ;
        else if ( Build.BOARD.contains ( "sapphire"    ) ) iDeviceModel = kDeviceModelHTCMagic           ;
        else if ( Build.BOARD.contains ( "liberty"     ) ) iDeviceModel = kDeviceModelHTCAria            ;
        else if ( Build.BOARD.contains ( "bahamas"     ) ) iDeviceModel = kDeviceModelHTCTatoo           ;
        else if ( Build.BOARD.contains ( "legend"      ) ) iDeviceModel = kDeviceModelHTCLegend          ;
        else if ( Build.BOARD.contains ( "inc"         ) ) iDeviceModel = kDeviceModelHTCDroidIncredible ;
        else if ( Build.BOARD.contains ( "desirec"     ) ) iDeviceModel = kDeviceModelHTCDroidEris       ;
        else if ( Build.BOARD.contains ( "supersonic"  ) ) iDeviceModel = kDeviceModelHTCEvo4G           ;
        else                                               iDeviceModel = kDeviceModelUnknown            ;
    }
    
    //------------------------------------------------------------------
    // Resume handling.
    //        
    public void onResume ( )
    {
        super.onResume ( ) ;

        queueEvent ( new Runnable ( ) { public void run ( ) { if ( oRenderer != null ) oRenderer.onResume ( ) ; } } ) ;
    }        

    //------------------------------------------------------------------
    // Pause handling.
    //        
    public void onPause ( )
    {
        super.onPause ( ) ; 
        
		Intent pleoConnectionServiceIntent = new Intent(_context,PleoConnectionService.class);
		pleoConnectionServiceIntent.setAction(PleoConnectionService.LOAD_PLEO_BEHAVIOR_TRY_ACTION);
		_context.startService(pleoConnectionServiceIntent);

        queueEvent ( new Runnable ( ) { public void run ( ) { if ( oRenderer != null ) oRenderer.onPause ( ) ; } } ) ;
    }        

    //------------------------------------------------------------------
    // Terminate handling.
    //        
    public void onTerminate ( )
    {
        queueEvent ( new Runnable ( ) { public void run ( ) { if ( oRenderer != null ) oRenderer.onShutdown ( ) ; } } ) ;
    }        

    //------------------------------------------------------------------
    // Restart handling.
    //        
    public void onRestart ( )
    {
        queueEvent ( new Runnable ( ) { public void run ( ) { if ( oRenderer != null ) oRenderer.onRestart ( ) ; } } ) ;
    }        

    //------------------------------------------------------------------
    // Text input handling
    //
    /*
    @Override 
    public boolean onCheckIsTextEditor ( )
    {
        return true ;
    }
    
    //------------------------------------------------------------------
    @Override 
    public InputConnection onCreateInputConnection ( EditorInfo outAttrs )
    {
        outAttrs.imeOptions |= EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_ACTION_NONE ;
        return new BaseInputConnection ( this, false ) ;
    }
    */
    
    //------------------------------------------------------------------
    /*
    public void onClick ( View v )
    {
        Log.d ( "#########", "onClick" ) ;
    }
    */

    //------------------------------------------------------------------
    // Location input event.
    //        
    public void onLocationChanged ( Location location )
    {
        //Log.d ( "########", "Location: " + location.getLatitude ( ) + ", " + location.getLongitude ( ) + ", " + location.getAltitude ( ) ) ;

        final float x = (float)location.getLatitude  ( ) ;
        final float y = (float)location.getLongitude ( ) ;
        final float z = (float)location.getAltitude  ( ) ;
        
        queueEvent ( new Runnable ( ) { public void run ( ) { if ( oRenderer != null ) oRenderer.onLocationEvent ( x, y, z ) ; } } ) ;
    }

    //------------------------------------------------------------------
    public void onProviderDisabled ( String provider )
    {
        //Log.d ( "#########", "Location provider disabled: " + provider.toString() ) ;
    }
    
    //------------------------------------------------------------------
    public void  onProviderEnabled ( String provider )
    {
        //Log.d ( "#########", "Location provider enabled: " + provider.toString() ) ;
    }
    
    //------------------------------------------------------------------
    public void onStatusChanged ( String provider, int status, Bundle extras )
    {
        //Log.d ( "#########", "Location provider status: " + provider.toString() ) ;
    }

    //------------------------------------------------------------------
    // Sensor input event.
    //        
    public void onAccuracyChanged ( Sensor arg0, int arg1 )
    {
    }

    //------------------------------------------------------------------

    public void onSensorChanged ( SensorEvent event )
    {
        if ( event.sensor.getType ( ) == Sensor.TYPE_ACCELEROMETER )
        {
            final float x = event.values[0] ;
            final float y = event.values[1] ;
            final float z = event.values[2] ;
            
            queueEvent ( new Runnable ( ) { public void run ( ) { if ( oRenderer != null ) oRenderer.onAccelerometerEvent ( x, y, z ) ; } } ) ;
        }
        else if ( event.sensor.getType ( ) == Sensor.TYPE_ORIENTATION )
        {
            final float azimuth =  event.values[0] ;
            @SuppressWarnings("unused")
			final float pitch   =  event.values[1] ;
            @SuppressWarnings("unused")
			final float roll    = -event.values[2] ;

            queueEvent ( new Runnable ( ) { public void run ( ) { if ( oRenderer != null ) oRenderer.onHeadingEvent ( azimuth ) ; } } ) ;
        }
        //else if ( event.sensor.getType ( ) == Sensor.TYPE_MAGNETIC_FIELD )
        //{
        //    final float x = event.values[0] ;
        //    final float y = event.values[1] ;
        //    final float z = event.values[2] ;
        //}
    }
         
    //------------------------------------------------------------------
    // Touch input event.
    //        
    public boolean onTouchEvent ( final MotionEvent event )
    {
        super.onTouchEvent ( event ) ;
        
        queueEvent
         (
            new Runnable ( )
            {
                public void run ( )
                {
                    if ( oRenderer != null ) 
                    {
                        int      act = event.getAction       ( ) ;
                        int      cnt = event.getPointerCount ( ) ;
                        switch ( cnt )
                        {
                        case 0  : oRenderer.onTouchEvent ( act, cnt,                     0,                0,                0  ,
                                                                                         0,                0,                0  ,
                                                                                         0,                0,                0  ,
                                                                                         0,                0,                0  ,
                                                                                         0,                0,                0   ) ; break ;
                        case 1  : oRenderer.onTouchEvent ( act, cnt, event.getPressure ( 0 ), event.getX ( 0 ), event.getY ( 0 ),
                                                                                         0,                0,                0  ,
                                                                                         0,                0,                0  ,
                                                                                         0,                0,                0  ,
                                                                                         0,                0,                0   ) ; break ;
                        case 2  : oRenderer.onTouchEvent ( act, cnt, event.getPressure ( 0 ), event.getX ( 0 ), event.getY ( 0 ),
                                                                     event.getPressure ( 1 ), event.getX ( 1 ), event.getY ( 1 ),
                                                                                         0,                0,                0  ,
                                                                                         0,                0,                0  ,
                                                                                         0,                0,                0   ) ; break ;
                        case 3  : oRenderer.onTouchEvent ( act, cnt, event.getPressure ( 0 ), event.getX ( 0 ), event.getY ( 0 ),
                                                                     event.getPressure ( 1 ), event.getX ( 1 ), event.getY ( 1 ),
                                                                     event.getPressure ( 2 ), event.getX ( 2 ), event.getY ( 2 ),
                                                                                         0,                0,                0  ,
                                                                                         0,                0,                0   ) ; break ;
                        case 4  : oRenderer.onTouchEvent ( act, cnt, event.getPressure ( 0 ), event.getX ( 0 ), event.getY ( 0 ),
                                                                     event.getPressure ( 1 ), event.getX ( 1 ), event.getY ( 1 ),
                                                                     event.getPressure ( 2 ), event.getX ( 2 ), event.getY ( 2 ),
                                                                     event.getPressure ( 3 ), event.getX ( 3 ), event.getY ( 3 ),
                                                                                         0,                0,                0   ) ; break ;
                        default : oRenderer.onTouchEvent ( act, cnt, event.getPressure ( 0 ), event.getX ( 0 ), event.getY ( 0 ),
                                                                     event.getPressure ( 1 ), event.getX ( 1 ), event.getY ( 1 ),
                                                                     event.getPressure ( 2 ), event.getX ( 2 ), event.getY ( 2 ),
                                                                     event.getPressure ( 3 ), event.getX ( 3 ), event.getY ( 3 ),
                                                                     event.getPressure ( 4 ), event.getX ( 4 ), event.getY ( 4 ) ) ; break ;
                        }
                    }
                }
            }
        ) ;
        return true ;
    }

    //------------------------------------------------------------------
    // Keyboard input events.
    //
    public boolean onKeyDown ( final int keyCode, KeyEvent event )
    {
        final int uniCode = event.getUnicodeChar ( ) ;
                        
        //Log.d ( "S3DRenderer", "### onKeyDown: " + keyCode ) ;
        {
            queueEvent
            (
                new Runnable ( )
                {
                    public void run ( )
                    {
                        if ( oRenderer != null ) oRenderer.onKeyEvent ( keyCode, uniCode, true ) ;
                    }
                }
            ) ;
        }
        return true ;
    }
    public boolean onKeyUp ( final int keyCode, KeyEvent event )
    {
        final int uniCode = event.getUnicodeChar ( ) ;
        
        queueEvent
        (
            new Runnable ( )
            {
                public void run ( )
                {
                    if ( oRenderer != null ) oRenderer.onKeyEvent ( keyCode, uniCode, false ) ;
                }
            }
        ) ;
        return true ;
    }

    //------------------------------------------------------------------
    // Movie events.
    //
	public void onOverlayMovieStopped ( )
	{
		queueEvent ( new Runnable ( ) { public void run ( ) { if ( oRenderer != null ) oRenderer.onOverlayMovieStopped ( ) ; } } ) ;
	}

    //------------------------------------------------------------------
    // OpenGL initialization helpers
    //
    private void initOpenGLES ( boolean _bWantGLES2 )
    {
        // Search for device specific preferences.
        //
        int iPreferedRedSize        = 8 ;
        int iPreferedGreenSize      = 8 ;
        int iPreferedBlueSize       = 8 ;
        int iPreferedAlphaSize      = 8 ; // For rendermaps and/or 3D overlay
        int iPreferedDepthSize      = 16 ;
        int iPreferedStencilSize    = 0 ;
            
        if ( _bWantGLES2 )
        {
            /* By default, GLSurfaceView() creates a RGB_565 opaque surface.
             * If we want a translucent one, we should change the surface's
             * format here, using PixelFormat.TRANSLUCENT for GL Surfaces
             * is interpreted as any 32-bit surface with alpha by SurfaceFlinger.
             */
             if ( iPreferedAlphaSize > 0 )
             {
                this.getHolder ( ).setFormat ( PixelFormat.TRANSLUCENT ) ;
             }

            /* Setup the context factory for 2.0 rendering.
             * See ContextFactory class definition below
             */
            setEGLContextFactory ( new S3DContextFactoryGLES2 ( ) ) ;

            /* We need to choose an EGLConfig that matches the format of
             * our surface exactly. This is going to be done in our
             * custom config chooser. See ConfigChooser class definition
             * below.
             */
            setEGLConfigChooser ( new S3DConfigChooserGLES2 ( iPreferedRedSize, iPreferedGreenSize, iPreferedBlueSize, iPreferedAlphaSize, iPreferedDepthSize, iPreferedStencilSize ) ) ;
        }
        /*
        else if ( iDeviceModel == kDeviceModelHTCHero           || 
                  iDeviceModel == kDeviceModelSamsungCorby      ||
                  iDeviceModel == kDeviceModelSamsungGalaxy     || 
                  iDeviceModel == kDeviceModelSamsungGalaxySpica )
        {
            setEGLConfigChooser ( new GLSurfaceView.EGLConfigChooser ( ) 
                                {
                        			public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) 
                        			{
                        				// Ensure that we get a 16bit framebuffer. Otherwise, we'll fall
                        				// back to Pixelflinger on some device (read: Samsung I7500)
                        				int[] attributes = new int[] { EGL10.EGL_DEPTH_SIZE, 16, EGL10.EGL_NONE };
                        				EGLConfig[] configs = new EGLConfig[1];
                        				int[] result = new int[1];
                        				egl.eglChooseConfig(display, attributes, configs, 1, result);
                        				return configs[0];
                        			}
		                        } 
		                        ) ;
        }
        */
        else
        {
            /* By default, GLSurfaceView() creates a RGB_565 opaque surface.
             * If we want a translucent one, we should change the surface's
             * format here, using PixelFormat.TRANSLUCENT for GL Surfaces
             * is interpreted as any 32-bit surface with alpha by SurfaceFlinger.
             */
             if ( iPreferedAlphaSize > 0 )
             {
                 this.getHolder ( ).setFormat ( PixelFormat.TRANSLUCENT ) ;
             }

            /* We need to choose an EGLConfig that matches the format of
             * our surface exactly. This is going to be done in our
             * custom config chooser. See ConfigChooser class definition
             * below.
             */
            setEGLConfigChooser ( new S3DConfigChooserGLES1 ( iPreferedRedSize, iPreferedGreenSize, iPreferedBlueSize, iPreferedAlphaSize, iPreferedDepthSize, iPreferedStencilSize ) ) ;
        }
    }
    
    //------------------------------------------------------------------
    // Renderer instance.
    //
    private S3DRenderer     oRenderer ;
    private Context _context;
        
    //------------------------------------------------------------------
    // Some other vars.
    //
    private String          sCacheDirPath   ;
    private String          sHomeDirPath    ;
    private String          sPackDirPath    ;
    @SuppressWarnings("unused")
	private int             iDeviceModel    ;
}


//----------------------------------------------------------------------
class S3DRenderer implements GLSurfaceView.Renderer
{
    //------------------------------------------------------------------
    // Constructor.
    //
    public S3DRenderer ( Context context, String sCacheDirPath, String sHomeDirPath, String sPackDirPath, boolean bForceDefaultOrientation )
    {
        engineSetLocationSupport        ( true ) ; //??? Force it for now, as I don't know a synchronous way to detect is accurately...
        engineSetHeadingSupport         ( MyPleo.areHeadingUpdatesSupported ( ) ) ;
        //engineSetDeviceName             (  ) ; 
        engineSetDeviceModel            ( Build.MODEL ) ; 
        engineSetSystemVersion          ( Build.VERSION.RELEASE ) ; 
        engineSetSystemLanguage         ( Locale.getDefault ( ).getLanguage ( ) ) ; 
        engineSetDirectories            ( sCacheDirPath, sHomeDirPath, sPackDirPath ) ;
        engineForceDefaultOrientation   ( bForceDefaultOrientation ) ;
        oContext = context ;
        bPaused  = false ;
    }

    //------------------------------------------------------------------
    // Pause handling
    //
    public void onPause ( )
    {
        bPaused = true ;
    }

    //------------------------------------------------------------------
    // Resume handling
    //
    public void onResume ( )
    {
        bPaused = false ;
    }

    //------------------------------------------------------------------
    // Engine shutdown.
    //
    public void onShutdown ( )
    {
        engineShutdown ( ) ;
    }

    //------------------------------------------------------------------
    // Engine restart.
    //
    public void onRestart ( )
    {
        engineShutdown    ( ) ;
        engineInitialize  ( ) ;
    }

    //------------------------------------------------------------------
    // Surface creation.
    //
    public void onSurfaceCreated ( GL10 gl, EGLConfig config )
    {
        onRestart ( ) ;

		// Hide splash sceen when 3D surface has been successfully created
		//
		Message.obtain ( ((MyPleo)oContext).oUIHandler, MyPleo.MSG_HIDE_SPLASH ).sendToTarget ( ) ;
    }

    //------------------------------------------------------------------
    // Surface resize handling.
    //
    public void onSurfaceChanged ( GL10 gl, int w, int h )
    {
        engineOnResize ( w, h ) ;
    }

    //------------------------------------------------------------------
    // Drawing function.
    //
    public void onDrawFrame ( GL10 gl )
    {
        if ( ! bPaused )
        {
            if ( engineRunOneFrame ( ) )
			{
				// Overlay movie handling
				//
				String sOverlayMovieToPlay  = engineGetOverlayMovie ( ) ;
				if   ( sOverlayMovieToPlay != sOverlayMovie ) 
				{
					sOverlayMovie = sOverlayMovieToPlay ;
					if ( sOverlayMovie.length ( ) > 0 ) Message.obtain ( ((MyPleo)oContext).oUIHandler, MyPleo.MSG_PLAY_OVERLAY_MOVIE, sOverlayMovie ).sendToTarget ( ) ;
				}
				
				// Vibrator handling
				//
				//boolean bVibratorState  = engineGetVibratorState ( ) ;
				//if    ( bVibratorState != bVibrate )
				//{
				//	bVibrate = bVibratorState ;
				//	Message.obtain ( ((MyPleo)oContext).oUIHandler, MyPleo.MSG_ENABLE_VIBRATOR, bVibrate ? 1 : 0, 0 ).sendToTarget ( ) ;
				//}
			}
			else
            {
                ((Activity) oContext).finish ( ) ;
            }
            //Log.d ( "S3DRenderer", "### onDrawFrame" ) ;
        }
    }

    //------------------------------------------------------------------
    // Keyboard input handling.
    //
    public void onKeyEvent ( int keyCode, int uniCode, boolean keyDown )
    {
        if ( keyDown )  engineOnKeyboardKeyDown ( keyCode, uniCode ) ;
        else            engineOnKeyboardKeyUp   ( keyCode, uniCode ) ;
    }

    //------------------------------------------------------------------
    // Touch input handling.
    //
    public void onTouchEvent ( int action, int cnt, float p0, float x0, float y0, float p1, float x1, float y1, float p2, float x2, float y2, float p3, float x3, float y3, float p4, float x4, float y4 )
    {
        boolean bNoMoreContacts = ( cnt == 0 ) ;
        
        if ( cnt == 1 )
        {
            switch ( action )
            {
                case MotionEvent.ACTION_MOVE   : engineOnMouseMove       ( x0, y0 ) ; break ;
                case MotionEvent.ACTION_DOWN   : engineOnMouseButtonDown ( x0, y0 ) ; break ;
                case MotionEvent.ACTION_UP     :
                case MotionEvent.ACTION_CANCEL : engineOnMouseButtonUp   ( x0, y0 ) ; bNoMoreContacts = true ; break ;
            }
        }
        
        if ( bNoMoreContacts )
        {
            engineOnTouchesChange ( 0, 0.0f, 0.0f, 0, 0.0f, 0.0f, 0, 0.0f, 0.0f, 0, 0.0f, 0.0f, 0, 0.0f, 0.0f ) ;
        }
        else
        {
            engineOnTouchesChange ( ( p0 > 0.01f ) ? 1 : 0, x0, y0,
                                    ( p1 > 0.01f ) ? 1 : 0, x1, y1,
                                    ( p2 > 0.01f ) ? 1 : 0, x2, y2,
                                    ( p3 > 0.01f ) ? 1 : 0, x3, y3,
                                    ( p4 > 0.01f ) ? 1 : 0, x4, y4 ) ;
        }
    }

    //------------------------------------------------------------------
    // Accelerometer input handling.
    //
    public void onAccelerometerEvent ( float x, float y, float z )
    {
        engineOnDeviceMove ( x, y, z ) ;
    }

    //------------------------------------------------------------------
    // Location input handling.
    //
    public void onLocationEvent ( float x, float y, float z )
    {
        engineOnLocationChanged ( x, y, z ) ;
    }

    //------------------------------------------------------------------
    // Heading input handling.
    //
    public void onHeadingEvent ( float angle )
    {
        engineOnHeadingChanged ( angle ) ;
    }

    //------------------------------------------------------------------
    // Movie handling.
    //
	public void onOverlayMovieStopped ( )
	{
		engineOnOverlayMovieStopped ( ) ;
	}

    //------------------------------------------------------------------
    // Member variables.
    //
    private boolean bPaused  		;
    private Context oContext 		;
	private String  sOverlayMovie 	= "" ;
	//private boolean bVibrate		= false ;

    //------------------------------------------------------------------
    // Engine native functions interface.
    //
    public native void      engineSetDirectories            	( String sCacheDirPath, String sHomeDirPath, String sPackDirPath ) ;
    public native void      engineSetLocationSupport        	( boolean b ) ;
    public native void      engineSetHeadingSupport         	( boolean b ) ;
    public native void      engineSetDeviceName         		( String sName ) ;
    public native void      engineSetDeviceModel        		( String sModel ) ;
    public native void      engineSetSystemVersion				( String sVersion ) ;
    public native void      engineSetSystemLanguage         	( String sLanguage ) ;
    public native boolean   engineInitialize                	( ) ;
    public native void      engineShutdown                  	( ) ;
    public native boolean   engineRunOneFrame               	( ) ;
    public native void      engineOnResize                  	( int w, int h ) ;
    public native void      engineOnMouseMove               	( float x, float y ) ;
    public native void      engineOnMouseButtonDown         	( float x, float y ) ;
    public native void      engineOnMouseButtonUp           	( float x, float y ) ;
    public native void      engineOnDeviceMove              	( float x, float y, float z ) ;
    public native void      engineOnKeyboardKeyDown         	( int key, int uni ) ;
    public native void      engineOnKeyboardKeyUp           	( int key, int uni ) ;
    public native void      engineOnTouchesChange           	( int tc1, float x1, float y1, int tc2, float x2, float y2, int tc3, float x3, float y3, int tc4, float x4, float y4, int tc5, float x5, float y5 ) ;
    public native void      engineOnLocationChanged         	( float x, float y, float z ) ;
    public native void      engineOnHeadingChanged          	( float angle ) ;
    public native void      engineForceDefaultOrientation   	( boolean b ) ;
    public native void      engineOnOverlayMovieStopped			( ) ;

    public native String 	engineGetOverlayMovie				( ) ;
	//public native boolean	engineGetVibratorState				( ) ;
}

//----------------------------------------------------------------------
class S3DContextFactoryGLES1 implements GLSurfaceView.EGLContextFactory 
{
    private static void checkEglError ( String prompt, EGL10 egl) 
    {
        int error;
        if ( ( error = egl.eglGetError ( ) ) != EGL10.EGL_SUCCESS )
        {
            Log.e("S3DContextFactory", String.format("%s: EGL error: 0x%x", prompt, error));
        }
    }

    private static int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
    public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig eglConfig) 
    {
        Log.w("S3DContextFactory", "creating OpenGL ES 1.1 context");
        checkEglError("Before eglCreateContext", egl);
        int[] attrib_list = {EGL_CONTEXT_CLIENT_VERSION, 1, EGL10.EGL_NONE };
        EGLContext context = egl.eglCreateContext(display, eglConfig, EGL10.EGL_NO_CONTEXT, attrib_list);
        checkEglError("After eglCreateContext", egl);
        return context;
    }

    public void destroyContext ( EGL10 egl, EGLDisplay display, EGLContext context ) 
    {
        egl.eglDestroyContext ( display, context ) ;
    }
}

//----------------------------------------------------------------------
class S3DContextFactoryGLES2 implements GLSurfaceView.EGLContextFactory 
{
    private static void checkEglError ( String prompt, EGL10 egl) 
    {
        int error;
        if ( ( error = egl.eglGetError ( ) ) != EGL10.EGL_SUCCESS )
        {
            Log.e("S3DContextFactory", String.format("%s: EGL error: 0x%x", prompt, error));
        }
    }

    private static int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
    public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig eglConfig) 
    {
        Log.w("S3DContextFactory", "creating OpenGL ES 2.0 context");
        checkEglError("Before eglCreateContext", egl);
        int[] attrib_list = {EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE };
        EGLContext context = egl.eglCreateContext(display, eglConfig, EGL10.EGL_NO_CONTEXT, attrib_list);
        checkEglError("After eglCreateContext", egl);
        return context;
    }

    public void destroyContext ( EGL10 egl, EGLDisplay display, EGLContext context ) 
    {
        egl.eglDestroyContext ( display, context ) ;
    }
}

//----------------------------------------------------------------------
class S3DConfigChooserGLES1 implements GLSurfaceView.EGLConfigChooser 
{
    private static final boolean DEBUG = false;
    
    public S3DConfigChooserGLES1 ( int r, int g, int b, int a, int depth, int stencil ) 
    {
        mRedSize        = r;
        mGreenSize      = g;
        mBlueSize       = b;
        mAlphaSize      = a;
        mDepthSize      = depth;
        mStencilSize    = stencil;
    }

    /* This EGL config specification is used to specify 1.0 rendering.
     * We use a minimum size of 4 bits for red/green/blue, but will
     * perform actual matching in chooseConfig() below.
     */
    //private static int EGL_OPENGL_ES_BIT = 1;
    private static int[] s_configAttribs2 =
    {
        //EGL10.EGL_RED_SIZE, 4,
        //EGL10.EGL_GREEN_SIZE, 4,
        //EGL10.EGL_BLUE_SIZE, 4,
        //EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES_BIT,
		EGL10.EGL_DEPTH_SIZE, 16,
        EGL10.EGL_NONE
    };

    public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {

        /* Get the number of minimally matching EGL configurations
         */
        int[] num_config = new int[1];
        egl.eglChooseConfig(display, s_configAttribs2, null, 0, num_config);

        int numConfigs = num_config[0];

        if (numConfigs <= 0) {
            throw new IllegalArgumentException("No configs match configSpec");
        }

        /* Allocate then read the array of minimally matching EGL configs
         */
        EGLConfig[] configs = new EGLConfig[numConfigs];
        egl.eglChooseConfig(display, s_configAttribs2, configs, numConfigs, num_config);

        if (DEBUG) {
             printConfigs(egl, display, configs);
        }
        /* Now return the "best" one
         */
        return chooseConfig(egl, display, configs);
    }

    public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display, EGLConfig[] configs) 
    {
        for(EGLConfig config : configs) 
        {
            int d = findConfigAttrib ( egl, display, config, EGL10.EGL_DEPTH_SIZE,   0 ) ;
            int s = findConfigAttrib ( egl, display, config, EGL10.EGL_STENCIL_SIZE, 0 ) ;

            // We need at least mDepthSize and mStencilSize bits
            if (d < mDepthSize || s < mStencilSize)
                continue;

            // We want an *exact* match for red/green/blue/alpha
            int r = findConfigAttrib ( egl, display, config, EGL10.EGL_RED_SIZE,   0 ) ;
            int g = findConfigAttrib ( egl, display, config, EGL10.EGL_GREEN_SIZE, 0 ) ;
            int b = findConfigAttrib ( egl, display, config, EGL10.EGL_BLUE_SIZE,  0 ) ;
            int a = findConfigAttrib ( egl, display, config, EGL10.EGL_ALPHA_SIZE, 0 ) ;

            if (r == mRedSize && g == mGreenSize && b == mBlueSize && a == mAlphaSize)
                return config;
        }
        // Fallback to RGB565 with 16bits depth
        //
        if ( mRedSize       != 5    &&
             mGreenSize     != 6    &&
             mBlueSize      != 5    &&
             mAlphaSize     != 0    &&
             mDepthSize     != 16   &&
             mStencilSize   != 0    )
        {     
            mRedSize        = 5;
            mGreenSize      = 6;
            mBlueSize       = 5;
            mAlphaSize      = 0;
            mDepthSize      = 16;
            mStencilSize    = 0;
            return chooseConfig(egl, display, configs);
        }
        
        // Really no luck :(
        //
        return null;
    }

    private int findConfigAttrib(EGL10 egl, EGLDisplay display, EGLConfig config, int attribute, int defaultValue) 
    {
        if (egl.eglGetConfigAttrib(display, config, attribute, mValue)) {
            return mValue[0];
        }
        return defaultValue;
    }

    private void printConfigs(EGL10 egl, EGLDisplay display, EGLConfig[] configs) 
    {
        int numConfigs = configs.length;
        Log.w("S3DConfigChooserGLES1", String.format("%d configurations", numConfigs));
        for (int i = 0; i < numConfigs; i++) {
            Log.w("S3DConfigChooserGLES1", String.format("Configuration %d:\n", i));
            printConfig(egl, display, configs[i]);
        }
    }

    private void printConfig(EGL10 egl, EGLDisplay display, EGLConfig config) 
    {
        int[] attributes = 
        {
                EGL10.EGL_BUFFER_SIZE,
                EGL10.EGL_ALPHA_SIZE,
                EGL10.EGL_BLUE_SIZE,
                EGL10.EGL_GREEN_SIZE,
                EGL10.EGL_RED_SIZE,
                EGL10.EGL_DEPTH_SIZE,
                EGL10.EGL_STENCIL_SIZE,
                EGL10.EGL_CONFIG_CAVEAT,
                EGL10.EGL_CONFIG_ID,
                EGL10.EGL_LEVEL,
                EGL10.EGL_MAX_PBUFFER_HEIGHT,
                EGL10.EGL_MAX_PBUFFER_PIXELS,
                EGL10.EGL_MAX_PBUFFER_WIDTH,
                EGL10.EGL_NATIVE_RENDERABLE,
                EGL10.EGL_NATIVE_VISUAL_ID,
                EGL10.EGL_NATIVE_VISUAL_TYPE,
                0x3030, // EGL10.EGL_PRESERVED_RESOURCES,
                EGL10.EGL_SAMPLES,
                EGL10.EGL_SAMPLE_BUFFERS,
                EGL10.EGL_SURFACE_TYPE,
                EGL10.EGL_TRANSPARENT_TYPE,
                EGL10.EGL_TRANSPARENT_RED_VALUE,
                EGL10.EGL_TRANSPARENT_GREEN_VALUE,
                EGL10.EGL_TRANSPARENT_BLUE_VALUE,
                0x3039, // EGL10.EGL_BIND_TO_TEXTURE_RGB,
                0x303A, // EGL10.EGL_BIND_TO_TEXTURE_RGBA,
                0x303B, // EGL10.EGL_MIN_SWAP_INTERVAL,
                0x303C, // EGL10.EGL_MAX_SWAP_INTERVAL,
                EGL10.EGL_LUMINANCE_SIZE,
                EGL10.EGL_ALPHA_MASK_SIZE,
                EGL10.EGL_COLOR_BUFFER_TYPE,
                EGL10.EGL_RENDERABLE_TYPE,
                0x3042 // EGL10.EGL_CONFORMANT
        };
        String[] names = 
        {
                "EGL_BUFFER_SIZE",
                "EGL_ALPHA_SIZE",
                "EGL_BLUE_SIZE",
                "EGL_GREEN_SIZE",
                "EGL_RED_SIZE",
                "EGL_DEPTH_SIZE",
                "EGL_STENCIL_SIZE",
                "EGL_CONFIG_CAVEAT",
                "EGL_CONFIG_ID",
                "EGL_LEVEL",
                "EGL_MAX_PBUFFER_HEIGHT",
                "EGL_MAX_PBUFFER_PIXELS",
                "EGL_MAX_PBUFFER_WIDTH",
                "EGL_NATIVE_RENDERABLE",
                "EGL_NATIVE_VISUAL_ID",
                "EGL_NATIVE_VISUAL_TYPE",
                "EGL_PRESERVED_RESOURCES",
                "EGL_SAMPLES",
                "EGL_SAMPLE_BUFFERS",
                "EGL_SURFACE_TYPE",
                "EGL_TRANSPARENT_TYPE",
                "EGL_TRANSPARENT_RED_VALUE",
                "EGL_TRANSPARENT_GREEN_VALUE",
                "EGL_TRANSPARENT_BLUE_VALUE",
                "EGL_BIND_TO_TEXTURE_RGB",
                "EGL_BIND_TO_TEXTURE_RGBA",
                "EGL_MIN_SWAP_INTERVAL",
                "EGL_MAX_SWAP_INTERVAL",
                "EGL_LUMINANCE_SIZE",
                "EGL_ALPHA_MASK_SIZE",
                "EGL_COLOR_BUFFER_TYPE",
                "EGL_RENDERABLE_TYPE",
                "EGL_CONFORMANT"
        };
        int[] value = new int[1];
        for (int i = 0; i < attributes.length; i++) 
        {
            int attribute = attributes[i];
            String name = names[i];
            if ( egl.eglGetConfigAttrib(display, config, attribute, value)) 
            {
                Log.w("S3DConfigChooserGLES1", String.format("  %s: %d\n", name, value[0]));
            } 
            else 
            {
                Log.w("S3DConfigChooserGLES1", String.format("  %s: failed\n", name));
                // LOOPS FOREVER ON HTC HERO: while (egl.eglGetError() != EGL10.EGL_SUCCESS);
            }
        }
    }

    // Subclasses can adjust these values:
    protected int mRedSize;
    protected int mGreenSize;
    protected int mBlueSize;
    protected int mAlphaSize;
    protected int mDepthSize;
    protected int mStencilSize;
    private int[] mValue = new int[1];
}

//----------------------------------------------------------------------
class S3DConfigChooserGLES2 implements GLSurfaceView.EGLConfigChooser 
{
    private static final boolean DEBUG = true;
    
    public S3DConfigChooserGLES2 ( int r, int g, int b, int a, int depth, int stencil ) 
    {
        mRedSize        = r;
        mGreenSize      = g;
        mBlueSize       = b;
        mAlphaSize      = a;
        mDepthSize      = depth;
        mStencilSize    = stencil;
    }

    /* This EGL config specification is used to specify 2.0 rendering.
     * We use a minimum size of 4 bits for red/green/blue, but will
     * perform actual matching in chooseConfig() below.
     */
    private static int EGL_OPENGL_ES2_BIT = 4;
    private static int[] s_configAttribs2 =
    {
        EGL10.EGL_RED_SIZE, 		4,
        EGL10.EGL_GREEN_SIZE, 		4,
        EGL10.EGL_BLUE_SIZE, 		4,
        EGL10.EGL_RENDERABLE_TYPE,  EGL_OPENGL_ES2_BIT,
		EGL10.EGL_DEPTH_SIZE, 		16,
        EGL10.EGL_NONE
    };

    public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {

        /* Get the number of minimally matching EGL configurations
         */
        int[] num_config = new int[1];
        egl.eglChooseConfig(display, s_configAttribs2, null, 0, num_config);

        int numConfigs = num_config[0];

        if (numConfigs <= 0) {
            throw new IllegalArgumentException("No configs match configSpec");
        }

        /* Allocate then read the array of minimally matching EGL configs
         */
        EGLConfig[] configs = new EGLConfig[numConfigs];
        egl.eglChooseConfig(display, s_configAttribs2, configs, numConfigs, num_config);

        if (DEBUG) {
             printConfigs(egl, display, configs);
        }
        /* Now return the "best" one
         */
        return chooseConfig(egl, display, configs);
    }

    public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display, EGLConfig[] configs) 
    {
        for(EGLConfig config : configs) 
        {
            int d = findConfigAttrib ( egl, display, config, EGL10.EGL_DEPTH_SIZE,   0 ) ;
            int s = findConfigAttrib ( egl, display, config, EGL10.EGL_STENCIL_SIZE, 0 ) ;

            // We need at least mDepthSize and mStencilSize bits
            if (d < mDepthSize || s < mStencilSize)
                continue;

            // We want an *exact* match for red/green/blue/alpha
            int r = findConfigAttrib ( egl, display, config, EGL10.EGL_RED_SIZE,   0 ) ;
            int g = findConfigAttrib ( egl, display, config, EGL10.EGL_GREEN_SIZE, 0 ) ;
            int b = findConfigAttrib ( egl, display, config, EGL10.EGL_BLUE_SIZE,  0 ) ;
            int a = findConfigAttrib ( egl, display, config, EGL10.EGL_ALPHA_SIZE, 0 ) ;

            if (r == mRedSize && g == mGreenSize && b == mBlueSize && a == mAlphaSize)
                return config;                
        }
        // Fallback to RGB565 with 16bits depth
        //
        if ( mRedSize       != 5    &&
             mGreenSize     != 6    &&
             mBlueSize      != 5    &&
             mAlphaSize     != 0    &&
             mDepthSize     != 16   &&
             mStencilSize   != 0    )
        {     
            mRedSize        = 5;
            mGreenSize      = 6;
            mBlueSize       = 5;
            mAlphaSize      = 0;
            mDepthSize      = 16;
            mStencilSize    = 0;
            return chooseConfig(egl, display, configs);
        }
        
        // Really no luck :(
        //        
        return null;
    }

    private int findConfigAttrib(EGL10 egl, EGLDisplay display, EGLConfig config, int attribute, int defaultValue) 
    {
        if (egl.eglGetConfigAttrib(display, config, attribute, mValue)) {
            return mValue[0];
        }
        return defaultValue;
    }

    private void printConfigs(EGL10 egl, EGLDisplay display, EGLConfig[] configs) 
    {
        int numConfigs = configs.length;
        Log.w("S3DConfigChooserGLES2", String.format("%d configurations", numConfigs));
        for (int i = 0; i < numConfigs; i++) {
            Log.w("S3DConfigChooserGLES2", String.format("Configuration %d:\n", i));
            printConfig(egl, display, configs[i]);
        }
    }

    private void printConfig(EGL10 egl, EGLDisplay display, EGLConfig config) 
    {
        int[] attributes = 
        {
                EGL10.EGL_BUFFER_SIZE,
                EGL10.EGL_ALPHA_SIZE,
                EGL10.EGL_BLUE_SIZE,
                EGL10.EGL_GREEN_SIZE,
                EGL10.EGL_RED_SIZE,
                EGL10.EGL_DEPTH_SIZE,
                EGL10.EGL_STENCIL_SIZE,
                EGL10.EGL_CONFIG_CAVEAT,
                EGL10.EGL_CONFIG_ID,
                EGL10.EGL_LEVEL,
                EGL10.EGL_MAX_PBUFFER_HEIGHT,
                EGL10.EGL_MAX_PBUFFER_PIXELS,
                EGL10.EGL_MAX_PBUFFER_WIDTH,
                EGL10.EGL_NATIVE_RENDERABLE,
                EGL10.EGL_NATIVE_VISUAL_ID,
                EGL10.EGL_NATIVE_VISUAL_TYPE,
                0x3030, // EGL10.EGL_PRESERVED_RESOURCES,
                EGL10.EGL_SAMPLES,
                EGL10.EGL_SAMPLE_BUFFERS,
                EGL10.EGL_SURFACE_TYPE,
                EGL10.EGL_TRANSPARENT_TYPE,
                EGL10.EGL_TRANSPARENT_RED_VALUE,
                EGL10.EGL_TRANSPARENT_GREEN_VALUE,
                EGL10.EGL_TRANSPARENT_BLUE_VALUE,
                0x3039, // EGL10.EGL_BIND_TO_TEXTURE_RGB,
                0x303A, // EGL10.EGL_BIND_TO_TEXTURE_RGBA,
                0x303B, // EGL10.EGL_MIN_SWAP_INTERVAL,
                0x303C, // EGL10.EGL_MAX_SWAP_INTERVAL,
                EGL10.EGL_LUMINANCE_SIZE,
                EGL10.EGL_ALPHA_MASK_SIZE,
                EGL10.EGL_COLOR_BUFFER_TYPE,
                EGL10.EGL_RENDERABLE_TYPE,
                0x3042 // EGL10.EGL_CONFORMANT
        };
        String[] names = 
        {
                "EGL_BUFFER_SIZE",
                "EGL_ALPHA_SIZE",
                "EGL_BLUE_SIZE",
                "EGL_GREEN_SIZE",
                "EGL_RED_SIZE",
                "EGL_DEPTH_SIZE",
                "EGL_STENCIL_SIZE",
                "EGL_CONFIG_CAVEAT",
                "EGL_CONFIG_ID",
                "EGL_LEVEL",
                "EGL_MAX_PBUFFER_HEIGHT",
                "EGL_MAX_PBUFFER_PIXELS",
                "EGL_MAX_PBUFFER_WIDTH",
                "EGL_NATIVE_RENDERABLE",
                "EGL_NATIVE_VISUAL_ID",
                "EGL_NATIVE_VISUAL_TYPE",
                "EGL_PRESERVED_RESOURCES",
                "EGL_SAMPLES",
                "EGL_SAMPLE_BUFFERS",
                "EGL_SURFACE_TYPE",
                "EGL_TRANSPARENT_TYPE",
                "EGL_TRANSPARENT_RED_VALUE",
                "EGL_TRANSPARENT_GREEN_VALUE",
                "EGL_TRANSPARENT_BLUE_VALUE",
                "EGL_BIND_TO_TEXTURE_RGB",
                "EGL_BIND_TO_TEXTURE_RGBA",
                "EGL_MIN_SWAP_INTERVAL",
                "EGL_MAX_SWAP_INTERVAL",
                "EGL_LUMINANCE_SIZE",
                "EGL_ALPHA_MASK_SIZE",
                "EGL_COLOR_BUFFER_TYPE",
                "EGL_RENDERABLE_TYPE",
                "EGL_CONFORMANT"
        };
        int[] value = new int[1];
        for (int i = 0; i < attributes.length; i++) 
        {
            int attribute = attributes[i];
            String name = names[i];
            if ( egl.eglGetConfigAttrib(display, config, attribute, value)) 
            {
                Log.w("S3DConfigChooserGLES2", String.format("  %s: %d\n", name, value[0]));
            } 
            else 
            {
                Log.w("S3DConfigChooserGLES2", String.format("  %s: failed\n", name));
                // LOOPS FOREVER ON HTC HERO: while (egl.eglGetError() != EGL10.EGL_SUCCESS);
            }
        }
    }

    // Subclasses can adjust these values:
    protected int mRedSize;
    protected int mGreenSize;
    protected int mBlueSize;
    protected int mAlphaSize;
    protected int mDepthSize;
    protected int mStencilSize;
    private int[] mValue = new int[1];
}