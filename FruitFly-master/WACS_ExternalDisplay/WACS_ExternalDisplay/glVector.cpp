#include "stdafx.h"
#include "glVector.h"

glVector::glVector()
{
	i = j = k = 0.0f;
}

glVector::~glVector()
{

}

void glVector::operator *=(GLfloat scalar)
{
	i *= scalar;
	j *= scalar;
	k *= scalar;
}
