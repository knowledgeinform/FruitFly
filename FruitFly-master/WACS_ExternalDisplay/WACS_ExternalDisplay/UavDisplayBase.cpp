
#include "stdafx.h"
#include "UavDisplayBase.h"

UavDisplayBase::UavDisplayBase()
{
	
}

void UavDisplayBase::init ()
{
	readConfig();
	displayList = -1;

	quadricObj = gluNewQuadric();
	gluQuadricDrawStyle(quadricObj, GLU_FILL);
	gluQuadricNormals(quadricObj, GLU_SMOOTH);

	generateCallList();
}
		
UavDisplayBase::~UavDisplayBase()
{
	gluDeleteQuadric(quadricObj);
}

void UavDisplayBase::displayUav()
{
	glCallList (displayList);
}

