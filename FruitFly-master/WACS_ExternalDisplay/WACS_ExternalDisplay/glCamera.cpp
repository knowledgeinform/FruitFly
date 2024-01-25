#include "stdafx.h"
#include "glCamera.h"


glCamera::glCamera()
{
	// Initalize all our member varibles.
	m_MaxPitchRate			= 0.0f;
	m_MaxHeadingRate		= 0.0f;
	m_HeadingDegrees		= 0.0f;
	m_PitchDegrees			= 0.0f;
	m_MaxForwardVelocity	= 0.0f;
	m_ForwardVelocity		= 0.0f;
	cosHeading = 0;
	sinHeading = 0;
	cosPitch = 0;
	sinPitch = 0;
}

glCamera::~glCamera() {

}

void glCamera::SetPerspective( bool position ) {
	GLfloat Matrix[16];
	glQuaternion q;

	// Make the Quaternions that will represent our rotations
	m_qPitch.CreateFromAxisAngle(1.0f, 0.0f, 0.0f, m_PitchDegrees);
	m_qHeading.CreateFromAxisAngle(0.0f, 0.0f, -1.0f, m_HeadingDegrees);
	
	// Combine the pitch and heading rotations and store the results in q
	q = m_qPitch * m_qHeading;
	q.CreateMatrix(Matrix);

	// Let OpenGL set our new prespective on the world!
	glMultMatrixf(Matrix);
	
	// Create a matrix from the pitch Quaternion and get the j vector 
	// for our direction.
	m_qPitch.CreateMatrix(Matrix);
	m_DirectionVector.j = Matrix[9];

	// Combine the heading and pitch rotations and make a matrix to get
	// the i and j vectors for our direction.
	q = m_qHeading * m_qPitch;
	q.CreateMatrix(Matrix);
	m_DirectionVector.i = Matrix[8];
	m_DirectionVector.k = Matrix[10];

	// Scale the direction by our speed.
	m_DirectionVector *= m_ForwardVelocity;

	// Increment our position by the vector
	m_Position.x += m_DirectionVector.i;
	m_Position.y += m_DirectionVector.j;
	m_Position.z += m_DirectionVector.k;

	// Translate to our new position.
	if (position) glTranslatef(-m_Position.x, -m_Position.y, m_Position.z);
}

void glCamera::MovePerspective(float x, float y, float z) {
	GLfloat Matrix[16];
	glQuaternion q;

	// Make the Quaternions that will represent our rotations
	m_qPitch.CreateFromAxisAngle(1.0f, 0.0f, 0.0f, m_PitchDegrees);
	m_qHeading.CreateFromAxisAngle(0.0f, 0.0f, 1.0f, m_HeadingDegrees);
	
	// Combine the pitch and heading rotations and store the results in q
	q = m_qPitch * m_qHeading;
	q.CreateMatrix(Matrix);

	// Combine the heading and pitch rotations and make a matrix to get
	// the vectors for our direction.
	q = m_qHeading * m_qPitch;
	q.CreateMatrix(Matrix);

	// Increment our position by the vector
	m_Position.x += x*Matrix[0];
	m_Position.y += x*Matrix[1];
	m_Position.z += x*Matrix[2];

	// Increment our position by the vector
	m_Position.x += y*Matrix[4];
	m_Position.y += y*Matrix[5];
	m_Position.z += y*Matrix[6];

	// Increment our position by the vector
	m_Position.x += z*Matrix[8];
	m_Position.y += z*Matrix[9];
	m_Position.z += z*Matrix[10];
}

void glCamera::ChangePitch(GLfloat degrees) {
	m_PitchDegrees += degrees;

	// We don't want our pitch to run away from us. Although it
	// really doesn't matter I prefer to have my pitch degrees
	// within the range of -360.0f to 360.0f
	if(m_PitchDegrees > 360.0f)	{
		m_PitchDegrees -= 360.0f;
	} else if(m_PitchDegrees < -360.0f) {
		m_PitchDegrees += 360.0f;
	}
}

void glCamera::ChangeHeading(GLfloat degrees) {
	m_HeadingDegrees += degrees;

	// We don't want our heading to run away from us either. Although it
	// really doesn't matter I prefer to have my heading degrees
	// within the range of -360.0f to 360.0f
	if (m_HeadingDegrees > 360.0f) {
		m_HeadingDegrees -= 360.0f;
	} else if(m_HeadingDegrees < -360.0f) {
		m_HeadingDegrees += 360.0f;
	}
}

void glCamera::ChangeVelocity(GLfloat vel)
{
	if(fabs(vel) < fabs(m_MaxForwardVelocity))
	{
		// Our velocity is less than the max velocity increment that we 
		// defined so lets increment it.
		m_ForwardVelocity += vel;
	}
	else
	{
		// Our velocity is greater than the max velocity increment that
		// we defined so we can only increment our velocity by the 
		// maximum allowed value.
		if(vel < 0)
		{
			// We are slowing down so decrement
			m_ForwardVelocity -= -m_MaxForwardVelocity;
		}
		else
		{
			// We are speeding up so increment
			m_ForwardVelocity += m_MaxForwardVelocity;
		}
	}
}

void glCamera::updateTrig()
{	
	cosHeading = cos (m_HeadingDegrees*DEG2RAD);
	sinHeading = sin (m_HeadingDegrees*DEG2RAD);
	cosPitch = cos (m_PitchDegrees*DEG2RAD);
	sinPitch = sin (m_PitchDegrees*DEG2RAD);
}

void glCamera::calcFromOffsets(double xOffset, double yOffset, double zOffset, double estLatToMConv, double estLonToMConv)
{
	m_Position.x += (xOffset*estLonToMConv * (cosHeading) + yOffset*estLatToMConv * (sinHeading*cosPitch) + zOffset * (sinHeading*sinPitch))/estLonToMConv;
	m_Position.y += (xOffset*estLonToMConv * (-sinHeading) + yOffset*estLatToMConv * (cosHeading*cosPitch) + zOffset * (cosHeading*sinPitch))/estLatToMConv;
	m_Position.z += xOffset*estLonToMConv * (0) + yOffset*estLatToMConv * (-sinPitch) + zOffset * (cosPitch);
}