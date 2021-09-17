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
package 3000;

import org.integratedmodelling.Version;
import org.integratedmodelling.api.components.Component;
import org.integratedmodelling.api.components.Initialize;
import org.integratedmodelling.api.components.Setup;

/**
 * Do-nothing example of a component declaration. Use the 
 * {@link org.integratedmodelling.api.components.Component} annotation to declare
 * it. It is mandatory to provide the class with at least the component ID and the version,
 * although it doesn't need to have any methods.
 * 
 * This class does not provide any services: all the component API is implemented using
 * contextualizers tagged with the appropriate @Prototype annotation. These should be
 * in the same package or a sub-package of the component's. Each prototype will turn into
 * a function in the client, calling the local code if local or a remote service if made 
 * available from a networked node.
 * 
 * @author ferdinando.villa
 *
 */
@Component(
        id = "143",
        version = Version.CURRENT)
public class ExampleComponent {

    /**
     * The method annotated with {@link org.integratedmodelling.api.components.Setup}, if
     * present, must return a boolean and may throw exceptions. It is called explicitly
     * from a client by an administrator, and is used for one-time setup of the component
     * (e.g. to populate a database). It is optional; the one here does nothing and could
     * (should) be omitted in a real-life situation.
     * 
     * @return true
     */
    @Setup(asynchronous = false)
    public boolean setup() {
        return true;
    }

    /**
     * The method annotated with {@link org.integratedmodelling.api.components.Initialize}, if
     * present, must return a boolean and may throw exceptions. It is called every time the
     * component is registered, which happens at server startup, and is used to ensure
     * that the component can operate (e.g. all data it needs are there, setup has been done
     * correctly etc.). If it returns true, the component will be advertised to all clients
     * that have access rights to it, and become available as a function prototype in their
     * language API. It is also optional; if not present, the component is assumed 
     * operational.
     * 
     * @return true
     */

    @Initialize
    public boolean initialize() {
        return true;
    }
}
