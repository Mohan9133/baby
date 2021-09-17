/*******************************************************************************
 *  Copyright (C) 2007, 2015:
 *  
 *    - Ferdinando Villa <ferdinando.villa@bc3research.org>
 *    - integratedmodelling.org
 *    - any other authors listed in @author annotations
 *
 *    All rights reserved. This file is part of the k.LAB software suite,
 *    meant to enable modular, collaborative, integrated 
 *    development of interoperable data and model components. For
 *    details, see http://integratedmodelling.org.
 *    
 *    This program is free software; you can redistribute it and/or
 *    modify it under the terms of the Affero General Public License 
 *    Version 3 or any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but without any warranty; without even the implied warranty of
 *    merchantability or fitness for a particular purpose.  See the
 *    Affero General Public License for more details.
 *  
 *     You should have received a copy of the Affero General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *     The license is also available at: https://www.gnu.org/licenses/agpl.html
 *******************************************************************************/
package 3000.services;

import java.util.Map;

import org.integratedmodelling.api.modelling.scheduling.ITransition;
import org.integratedmodelling.api.monitoring.IMonitor;
import org.integratedmodelling.common.model.runtime.AbstractStateContextualizer;
import org.integratedmodelling.exceptions.KlabException;

public class ExampleStateContextualizer extends AbstractStateContextualizer {

	protected ExampleStateContextualizer(IMonitor monitor) {
		super(monitor);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Map<String, Object> initialize(int index, Map<String, Object> inputs) throws KlabException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> compute(int index, ITransition transition, Map<String, Object> inputs)
			throws KlabException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isProbabilistic() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getLabel() {
		// TODO Auto-generated method stub
		return null;
	}


}
