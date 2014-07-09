#include "FCEffectRunner.h"
#include "cinder/Perlin.h"
#include "cinder/Rand.h"
#include "cinder/app/AppBasic.h"

#include "cinder/Perlin.h"
#include "cinder/Rand.h"

using namespace ci;
using namespace ci::app;
using namespace std;

class MyEffect;
typedef boost::shared_ptr< MyEffect > MyEffectRef;

class MyEffect : public FCEffect
{
public:
	static MyEffectRef create()
    {
        return ( MyEffectRef )( new MyEffect() );
    }
    MyEffect()
        : time (0) {
		mPerlin = Perlin();
	}

    float time;
	Perlin					mPerlin;

    void beginFrame(const FrameInfo& f)
    {
        const float speed = 1.0;
        time += f.timeDelta * speed;
    }

    void shader(ci::Vec3f& rgb, const PixelInfo& p)
    {
        float distance = p.point.length();
		Vec3f timepoint = p.point + Vec3f(0,0,time);
        float noiseval = mPerlin.fBm(timepoint);
		rgb = rgbToHSV(Color(0.2, 0.3, noiseval));
	}
};

class FCKinectEffect;
typedef boost::shared_ptr< FCKinectEffect > FCKinectEffectRef;

using namespace ci;
class FCKinectEffect : public FCEffect
{
public:
	static FCKinectEffectRef create()
    {
        return ( FCKinectEffectRef )( new FCKinectEffect() );
    }
    FCKinectEffect()
        : activated (false), time(0), mBrightness(.0) {
	}
	bool activated;
	float mBrightness;
	bool time;
    void beginFrame(const FrameInfo& f)
    {
        const float speed = 1.0;
        time += f.timeDelta * speed;
    }

    void shader(ci::Vec3f& rgb, const PixelInfo& p)
    {
		Color col = Color(CM_HSV,1.f,1.f,mBrightness);
		rgb = Vec3f(col.r,col.g,col.b);
	}
};