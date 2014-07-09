
#include "cinder/app/AppBasic.h"
#include "cinder/gl/Texture.h"
#include "cinder/params/Params.h"

#include "cinder/Camera.h"
#include "Kinect2.h"

#include "cinder/gl/Vbo.h"
#include "cinder/gl/Texture.h"
#include "cinder/gl/GlslProg.h"
#include "cinder/gl/Fbo.h"

#include "cinder/Text.h"
#include "cinder/Utilities.h"
#include "cinder/Sphere.h"

#include "FCEffectRunner.h"

#include "FCKinectEffect.h"

#include "cinder/qtime/MovieWriter.h"

static const int        cDepthWidth  = 512;
static const int        cDepthHeight = 424;
static const int        cColorWidth  = 1920;
static const int        cColorHeight = 1080;

const ci::Color			LASSO_COLOR	= ci::ColorA(1.f,0,0,.5);
#define NUM_KINECTS 1
#define NUM_LIGHTS 2

class LightObj;
typedef boost::shared_ptr< LightObj > LightObjRef;

struct effect {
	FCEffectRunnerRef effectRunnerRef;
	FCKinectEffectRef effectRef;
};

class LightObj {
  public:
	static LightObjRef create(int index)
    {
        return ( LightObjRef )( new LightObj(index) );
    }
	LightObj(int index )
		
	{
		// create label
		ci::TextLayout text; text.clear( ci::Color::white() ); text.setColor( ci::Color(0.5f, 0.5f, 0.5f) );
		try { text.setFont( ci::Font( "Futura-CondensedMedium", 18 ) ); } catch( ... ) { text.setFont( ci::Font( "Arial", 18 ) ); }
		text.addLine( "Light " + ci::toString(index));
		mLabelTex = ci::gl::Texture( text.render( true ) );
		intersecting = false;
		mSphere.setRadius(.3);
		mPos = Vec3f::zero();
		mSphere.setCenter(Vec3f::zero());
	}
	
	void draw() const
	{
		// draw box and frame
		ci::gl::color( ColorA(mEffect.effectRef->mBrightness,0,0,1) );
		ci::gl::draw(mSphere);
		ci::gl::draw(mLabelTex);
	}
	
	ci::ColorA		mFalseColor;
	ci::ColorA		mTrueColor;
	ci::Vec3f		mPos;
	ci::gl::Texture	mLabelTex;
	ci::Sphere		mSphere;
	bool			intersecting;
	effect			mEffect;
};


class Kinect2FadeCandyApp : public ci::app::AppBasic
{
public:
	void						draw();
	void						prepareSettings( ci::app::AppBasic::Settings* settings );
	void						setup();
	void						update();
private:
	Kinect2::DeviceRef			mDevice;
	Kinect2::Frame				mFrame;

	float							mFrameRate;
	bool							mFullScreen;
	ci::params::InterfaceGlRef		mParams;
	ci::CameraPersp					mCamera;
    ci::Vec3f						mEyePoint;
    ci::Vec3f						mLookAt;
    ci::Vec3f						mRotation;

	ci::Vec3f						mTestPos;

	std::vector<LightObjRef>		mLights;
	
    ci::gl::VboMesh					mVboMesh;
    ci::gl::GlslProg				mShader;
	
	ci::Matrix44f					mKinectTranslation;
	
	ci::Vec3f						mSceneRotate;

	UINT32	vecSize;

	bool mAutoRotate;

	Vec3f	mLeftHandStart;
	float	mLeftHandDist;

	float mLeftYChange;

};

#include "cinder/gl/gl.h"
#include "cinder/Utilities.h"

using namespace ci;
using namespace ci::app;
using namespace std;

void Kinect2FadeCandyApp::draw()
{
	 gl::setViewport( getWindowBounds() );
    gl::setMatricesWindow( getWindowSize() );

	 gl::clear( ColorAf::gray(.5,1), true );
    gl::color( ColorAf::white() );
   
    gl::enable( GL_DEPTH_TEST );
    gl::enable( GL_LINE_SMOOTH );
    glHint( GL_LINE_SMOOTH_HINT, GL_NICEST );
    gl::enable( GL_POINT_SMOOTH );
    glHint( GL_POINT_SMOOTH_HINT, GL_NICEST );
    gl::enable( GL_POLYGON_SMOOTH );
    glHint( GL_POLYGON_SMOOTH_HINT, GL_NICEST );

    gl::enableAlphaBlending();
	gl::enableDepthRead();
	gl::enableDepthWrite();
	
	 gl::pushMatrices();
	  
	if ( mFrame.getDepth() ) {
		gl::TextureRef tex = gl::Texture::create( Kinect2::mapDepthFrameToCamera(mFrame.getDepth(),mDevice->getCoordinateMapper() ) );
		//gl::draw( tex, tex->getBounds(), Rectf( getWindowBounds() ) );
		
		gl::pushMatrices();
		gl::setMatrices( mCamera );
		gl::rotate(mSceneRotate);
		
		gl::disable(GL_TEXTURE_2D);
		 
		Vec3f floorVec = - mDevice->getFloorPlane().xyz();
		Vec3f floorPoint = Vec3f(0,0,0) - floorVec;
		Vec3f perpVec = floorVec.cross(Vec3f(1,0,0));
		Vec3f otherPerpVec = perpVec.cross(floorVec);
		float boxSize = 100;
		 
		//draw floor
		TriMesh mesh;

		Vec3f oneSide = floorVec + (perpVec.normalized()*boxSize);
		Vec3f cornerPoint = oneSide + (otherPerpVec.normalized()*boxSize);
		mesh.appendVertex(cornerPoint);

		cornerPoint = oneSide - (otherPerpVec.normalized()*boxSize);
		mesh.appendVertex(cornerPoint);

		Vec3f otherSide = floorVec - (perpVec.normalized()*boxSize);

		cornerPoint = otherSide + (otherPerpVec.normalized()*boxSize);
		mesh.appendVertex(cornerPoint);

		cornerPoint = otherSide - (otherPerpVec.normalized()*.5);
		mesh.appendVertex(cornerPoint);

		mesh.appendTriangle(0, 1, 2);
		mesh.appendTriangle(1, 3, 2);

		gl::color(ColorA(0,1,.0,.2));
		gl::draw(mesh);
		gl::color(Color(0,1,0));
			//gl::drawVector(Vec3f(0,0,0),-mDevice->getFloorPlane().xyz());
			//gl::translate(floorPoint);
		//mDevice->getFloorPlane().xyz()
			float angleRads = mDevice->getFloorPlane().xyz().dot(perpVec);
			//gl::rotate(toDegrees(angleRads)); 
			gl::color(Color(0,.1,.8));
			//gl::drawCube(Vec3f(0,0,0),Vec3f(1,.1,1));
			
			//gl::drawCoordinateFrame();


		gl::enable( GL_TEXTURE_2D );

		//
		//bind shader and attach texture ids
		tex->bind(0);
		mShader.bind();
		
		mShader.uniform( "depthMap0", 0 );
		mShader.uniform( "clr", Color(1,0,0) );
		mShader.uniform( "brightness", 1.f );
		mShader.uniform( "transformMatrix0", mKinectTranslation );
		 
		mShader.uniform( "numDepthImages", 1 );
		mShader.uniform( "maxDepth", 1000 );
		mShader.uniform( "minDepth", 0 );
		mShader.uniform( "scaleFactorX", 1 );
		mShader.uniform( "scaleFactorY", 1 );

		gl::color( ColorA( 1, 1, 1, 1 ) );

		glPointSize( 1.0f );
		gl::draw( mVboMesh );
		//gl::drawColorCube(Vec3f::zero(),Vec3f(10,10,10));
		mShader.unbind();
		tex->unbind();

	}
	
	if ( mFrame.getBodyIndex() ) {
		Vec3f posHead;
		Vec3f posNeck;
		Color bodyColor;
		Ray leftRay;
		Ray rightRay;
		Vec3f leftHandVec;
		for ( const Kinect2::Body& body : mDevice->getFrame().getBodies() ) {
			
			for ( const auto& joint : body.getJointMap() ) {
				if(joint.second.getTrackingState() == TrackingState_Tracked){
					bodyColor = Kinect2::getBodyColor( body.getIndex() );
					gl::color( bodyColor );	
					gl::pushMatrices();
					gl::translate(joint.second.getPosition( ));
					gl::rotate(joint.second.getOrientation());
					gl::drawCoordinateFrame(.05,.0,.000);
					gl::popMatrices();
				}
				if(joint.first == JointType_HandLeft){
					int numIntersect = 0;
				
					if(body.getLeftHandState() == HandState_Closed){
						gl::color(LASSO_COLOR);
						gl::drawSphere(joint.second.getPosition(),.1);
					}
					Vec3f axis;
					float angle;
					joint.second.getOrientation().getAxisAngle(&axis,&angle);
					Vec3f pt(0,1,0);
					pt.rotate(axis,angle);
					Vec3f handVec = pt - Vec3f::zero();
					handVec.normalize();
					
					leftRay = Ray(joint.second.getPosition(),handVec);
					
					leftHandVec = joint.second.getPosition();

					Color drawColor = Color(0,0,1);
					
					for(auto l:mLights){
						if(l->mSphere.intersects(leftRay))
							drawColor = Color(1,0,0);
					}
					gl::color(drawColor);
					gl::drawVector(joint.second.getPosition(),joint.second.getPosition()+handVec);
				}
				if(joint.first == JointType_HandRight){
					if(body.getRightHandState() == HandState_Closed){
						gl::color(LASSO_COLOR);
						gl::drawSphere(joint.second.getPosition(),.1);
					}

					Vec3f axis;
					float angle;
					joint.second.getOrientation().getAxisAngle(&axis,&angle);
					Vec3f pt(0,1,0);
					pt.rotate(axis,angle);
					Vec3f handVec = pt - Vec3f::zero();
					handVec.normalize();
					rightRay = Ray(joint.second.getPosition(),handVec);
					Color drawColor = Color(0,0,1);
					
					for(auto l:mLights){
						if(l->mSphere.intersects(rightRay))
							drawColor = Color(1,0,0);
					}
					gl::color(drawColor);
					gl::drawVector(joint.second.getPosition(),joint.second.getPosition()+handVec);
					
				}
			}
		}
		for(auto l:mLights){
			bool lastIntersect = l->intersecting;
			l->intersecting = l->mSphere.intersects(rightRay);
			if(l->intersecting != lastIntersect && l->intersecting == true ){
				mLeftHandStart = leftHandVec;
				
			}
			
			else if (l->intersecting == true){
				mLeftHandDist = (leftHandVec-mLeftHandStart).length();
				gl::color(Color(0,1,0));
				gl::drawSphere(mLeftHandStart,.1);
				Vec3f	leftVec = (leftHandVec-mLeftHandStart);
				mLeftYChange = leftHandVec.y -mLeftHandStart.y;
				Vec3f	xVec = mLeftHandStart+Vec3f(1,0,0);
				gl::drawVector(mLeftHandStart,leftHandVec);
				float ang = toDegrees(leftVec.dot(xVec));
				console()<<"yChange " << mLeftYChange << endl;
			}

		}
		// clear out the window with black
		
		
	
			//gl::TextureRef tex = gl::Texture::create( Kinect2::colorizeBodyIndex( mFrame.getBodyIndex() ) );
		//gl::draw( tex, tex->getBounds(), Rectf( getWindowBounds() ) );
	}
	 	
	//gl::drawCoordinateFrame(.1,.01,.008);
	for(auto light : mLights){
		light->draw();
	}
	gl::color( Color(1,1,1) );

	for(auto l:mLights){
		l->mEffect.effectRunnerRef->draw();	
	}
	
	gl::popMatrices();
	gl::popMatrices();
	gl::disableAlphaBlending();
	gl::disableDepthRead();
	gl::disableDepthWrite();
	mParams->draw();
}

void Kinect2FadeCandyApp::prepareSettings( Settings* settings )
{
	settings->prepareWindow( Window::Format().size( 1280, 720 ).title( "Body App" ) );
	settings->setFrameRate( 60.0f );
}

void Kinect2FadeCandyApp::setup()
{
	
    // VBO data
    vector<uint32_t> vboIndices;
    gl::VboMesh::Layout vboLayout;
    vector<Vec3f> vboPositions;
    vector<Vec2f> vboTexCoords;

    // Set up VBO layout
    vboLayout.setStaticIndices();
    vboLayout.setStaticPositions();
    vboLayout.setStaticTexCoords2d();

    // VBO dimensions
    int32_t height = cDepthWidth;
    int32_t width = NUM_KINECTS * cDepthHeight;

    // Define VBO data
    for( int32_t x = 0; x < width; x++ )
    {
        for( int32_t y = 0; y < height; y++ )
        {
            vboIndices.push_back( ( uint32_t )( x * height + y ) );
            vboTexCoords.push_back( Vec2f( ( float )x / ( float )( width - 1 ), ( float )y / ( float )( height - 1 ) ) );
            vboPositions.push_back( Vec3f(
                                        ( vboTexCoords.rbegin()->x * 2.0f - 1.0f ) * ( float )width,
                                        ( vboTexCoords.rbegin()->y * 2.0f - 1.0f ) * ( float )height,
                                        0.0f ) );
        }
    }

    mVboMesh = gl::VboMesh( vboPositions.size(), vboIndices.size(), vboLayout, GL_POINTS );
    mVboMesh.bufferIndices( vboIndices );
    mVboMesh.bufferPositions( vboPositions );
    mVboMesh.bufferTexCoords2d( 0, vboTexCoords );
    mVboMesh.unbindBuffers();

    // Load shader
    try
    {
        mShader = gl::GlslProg( loadAsset( "kinectCalibrationView.vert" ), loadAsset( "kinectCalibrationView.frag" ) );
    }
    catch( gl::GlslProgCompileExc e )
    {
        console() << e.what() << endl;
    }

	mFrameRate	= 0.0f;
	mFullScreen	= false;
	mLookAt = Vec3f(0.f,0.f,0.f);
	mEyePoint = Vec3f(0.f,0.f,-5.f);
	mDevice = Kinect2::Device::create();
	mDevice->start( Kinect2::DeviceOptions().enableColor( false ).enableBody().enableBodyIndex() );
	
	console( ) << Kinect2::getDeviceCount() << " device(s) connected." << endl;
	map<size_t, string> deviceMap = Kinect2::getDeviceMap();

	for ( const auto& device : deviceMap ) {
		console( ) << "Index: " << device.first << ", ID: " << device.second << endl;
	}

	for(int i=0; i< NUM_LIGHTS; i++){
		LightObjRef light = LightObj::create(i);
		light->mFalseColor = ColorA(1,1,1,1);
		light->mTrueColor = ColorA(1,0,0,1);
		light->mPos = Vec3f(-.69,-.67,1.49);
		mLights.push_back(light);
	}
	mLights[0]->mPos = Vec3f(-2.86,-.55,.79);
	mLights[1]->mPos = Vec3f(-.94,-.45,1.51);
	mSceneRotate = Vec3f(0,0,0);
	mAutoRotate = true;
	mParams = params::InterfaceGl::create( "Params", Vec2i( 200, 100 ) );
	mParams->addParam( "Frame rate",	&mFrameRate,			"", true );
	mParams->addParam( "Full screen",	&mFullScreen,			"key=f" );
	mParams->addButton( "Quit", bind(	&Kinect2FadeCandyApp::quit, this ),	"key=q" );
	mParams->addParam( "lightpos 1", &mLights[0]->mPos);
	mParams->addParam( "lightpos 2", &mLights[1]->mPos);
	mParams->addParam( "mEyePoint", &mEyePoint);
	mParams->addParam( "mSceneRotate", &mSceneRotate);
	mParams->addParam( "mAutoRotate", &mAutoRotate);

	vecSize = cDepthWidth*cDepthHeight;
	mKinectTranslation = Matrix44f::identity();

	//setup fadecandy
	//point FC to host and port
	for(int i=0; i<NUM_LIGHTS; i++){
		effect e;
		e.effectRef = FCKinectEffect::create();
		e.effectRunnerRef = FCEffectRunner::create("127.0.0.1",7890);
		e.effectRunnerRef->setEffect(boost::dynamic_pointer_cast<FCEffect>(e.effectRef));
		e.effectRunnerRef->setMaxFrameRate(400);
		e.effectRunnerRef->setVerbose(true);
		e.effectRunnerRef->setChannelNum(i);
		e.effectRunnerRef->setLayout("layouts/strip25.json");
		//add visualizer to see effect on screen
		FCEffectVisualizerRef viz = FCEffectVisualizer::create();
		e.effectRunnerRef->setVisualizer(viz);
		mLights[i]->mEffect = e;
	}
	
	
	
	
}

void Kinect2FadeCandyApp::update()
{
	for(auto l:mLights){
		l->mEffect.effectRunnerRef->update();	
		l->mEffect.effectRef->activated = l->intersecting;
		if(l->intersecting)
		l->mEffect.effectRef->mBrightness += mLeftYChange/2.f;
		if(l->mEffect.effectRef->mBrightness > 1.)
			l->mEffect.effectRef->mBrightness = 1.;
		if(l->mEffect.effectRef->mBrightness < 0)
			l->mEffect.effectRef->mBrightness = 0;

	}
	
	mFrameRate = getAverageFps();
	
	if ( mFullScreen != isFullScreen() ) {
		setFullScreen( mFullScreen );
		mFullScreen = isFullScreen();
	}
	if ( mDevice && mDevice->getFrame().getTimeStamp() > mFrame.getTimeStamp() ) {
		mFrame = mDevice->getFrame();
	}

	mCamera.lookAt( mEyePoint, mLookAt );
    mCamera.setAspectRatio( 16.f/9.f );
	if(mAutoRotate)
	mSceneRotate.y += .2;

	for(auto light : mLights){
		light->mSphere.setCenter(light->mPos);
		//console()<<light->mSphere.getCenter()<<endl;
	}
}

CINDER_APP_BASIC( Kinect2FadeCandyApp, RendererGl )
