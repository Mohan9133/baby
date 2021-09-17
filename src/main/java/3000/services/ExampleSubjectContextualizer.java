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

import org.integratedmodelling.api.modelling.IActiveSubject;
import org.integratedmodelling.api.modelling.IModel;
import org.integratedmodelling.api.modelling.IObservable;
import org.integratedmodelling.api.modelling.IObservation;
import org.integratedmodelling.api.modelling.IState;
import org.integratedmodelling.api.modelling.contextualization.ISubjectContextualizer;
import org.integratedmodelling.api.modelling.resolution.IResolutionScope;
import org.integratedmodelling.api.modelling.scheduling.ITransition;
import org.integratedmodelling.api.monitoring.IMonitor;
import org.integratedmodelling.api.project.IProject;
import org.integratedmodelling.exceptions.KlabException;
import org.integratedmodelling.exceptions.KlabValidationException;

public class ExampleSubjectContextualizer implements ISubjectContextualizer {

    boolean dispose;

	@Override
	public boolean canDispose() {
		// TODO Auto-generated method stub
		return dispose;
	}

	@Override
	public void setContext(Map<String, Object> parameters, IModel model, IProject project)
			throws KlabValidationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<String, IObservation> initialize(IActiveSubject process, IActiveSubject contextSubject,
			IResolutionScope context, Map<String, IObservable> expectedInputs, Map<String, IObservable> expectedOutputs,
			IMonitor monitor) throws KlabException {
		// TODO Auto-generated method stub
		dispose = contextSubject.getScale().isTemporallyDistributed();
		return null;
	}

	@Override
	public Map<String, IObservation> compute(ITransition transition, Map<String, IState> inputs) throws KlabException {
		// TODO Auto-generated method stub
		dispose = transition.isLast();
		return null;
	}

}
