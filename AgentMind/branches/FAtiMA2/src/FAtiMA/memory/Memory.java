/** 
 * Memory.java - Performs operations that involve data from different memories - currently
 * 				AM and STM
 *  
 * Copyright (C) 2006 GAIPS/INESC-ID 
 *  
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 * Company: HWU
 * Project: LIREC
 * Created: 13/03/09 
 * @author: Meiyii Lim
 * Email to: myl@macs.hw.ac.uk
 * 
 * History: 
 * Meiyii Lim: 13/03/2009 - File created
 * 
 * **/

package FAtiMA.memory;


import FAtiMA.memory.episodicMemory.EpisodicMemory;
import FAtiMA.memory.semanticMemory.SemanticMemory;




/**
 * Performs operations that involve data from different memories - currently
 * AM and STM
 * 
 * @author Meiyii Lim
 */

public class Memory {

	/**
	 * for serialization purposes
	 */
	private static final long serialVersionUID = 1L;
	
	private SemanticMemory _sm;
	private EpisodicMemory _em;
	
	public Memory()
	{
		_sm = new SemanticMemory();
		_em = new EpisodicMemory();
	}
	
	public SemanticMemory getSemanticMemory()
	{
		return _sm;
	}
	
	public EpisodicMemory getEpisodicMemory()
	{
		return _em;
	}
	
	
	
}
