// Copyright (C) 2009 foam
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

#include "cv.h"
#include <string>

#ifndef IMAGE
#define IMAGE

class Image
{
public:
	Image(int w, int h, int d, int c);
	Image(const std::string &filename);
	Image(const Image *other);
	~Image();

	void PrintInfo();
	
	void Crop(int x, int y, int w, int h);
	void Scale(int w, int h);

	// Paste an image into this one
	void Blit(const Image &image, CvPoint pos);
	
	// Return a sum of squared differences, for giving a similarity metric 
	float SSD(Image &other);
	
	// Subtract the mean - this is useful for accounting for global lighting changes
	void SubMean();
	
	// Convert the image into a local binary patterns image
	void LBP();
	
	// Convert to different colour spaces
	void GRAY2RGB();
	void RGB2GRAY();
	void BayerGB2RGB();
	
	// Calculate a histogram for a given channel
	unsigned int *Hist(int channel);
	
	IplImage *m_Image;
	
private:
	unsigned char SafeGet2D(int y, int x, int c);
};

#endif