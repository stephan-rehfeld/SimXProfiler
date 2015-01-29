/*
 * Copyright 2015 Stephan Rehfeld
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package simx.profiler.discovery.latency;

import java.util.Objects;

/**
 *
 * @author Stephan Rehfeld
 */
public class ImmutableTuple< A, B > {
    
    public final A a;
    public final B b;

    public ImmutableTuple( final A a, final B b ) {
        this.a = a;
        this.b = b;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + Objects.hashCode(this.a);
        hash = 19 * hash + Objects.hashCode(this.b);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ImmutableTuple<?, ?> other = (ImmutableTuple<?, ?>) obj;
        if (!Objects.equals(this.a, other.a)) {
            return false;
        }
        return Objects.equals(this.b, other.b);
    }

    @Override
    public String toString() {
        return "ImmutableTuple{" + "a=" + a + ", b=" + b + '}';
    }
    
    
    
}
