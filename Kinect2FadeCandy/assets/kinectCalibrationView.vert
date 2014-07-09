// Inputs
uniform sampler2D depthMap0;
uniform sampler2D depthMap1;
uniform sampler2D depthMap2;

uniform mat4 transformMatrix0;
uniform mat4 transformMatrix1;
uniform mat4 transformMatrix2;

uniform float maxDepth;
uniform float minDepth;

// Properties
varying float brightness;
uniform vec3 color0;
uniform vec3 color1;
uniform vec3 color2;

varying vec3 clr;
varying vec4 vertex;

uniform float numDepthImages;
uniform float scaleFactorX;
uniform float scaleFactorY;

// Kernel
void main( void )
{
	float textureDivision = 1.0/numDepthImages;

	vec4 imageCoords = gl_MultiTexCoord0;
	
	vec3 newpos;
	vec4 colorval;
	colorval = texture2D( depthMap0, imageCoords.st );
	newpos = colorval.rgb;
	

	// Get position
	vec4 vertex;
	vertex.x = newpos.r;
	vertex.y = newpos.g;
	vertex.z = newpos.b;
	vertex.w = 1.0;

	// Transform position
	gl_Position = gl_ModelViewProjectionMatrix * vertex;
}

float map(float value, float min1, float max1, float min2, float max2)
{
     if(min1 == max1)
     {
          return value; 
     }    
     else
     {
          return (((value - min1) / (max1 - min1)) * (max2 - min2) + min2);
     }
}