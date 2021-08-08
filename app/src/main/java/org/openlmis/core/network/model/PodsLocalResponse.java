package org.openlmis.core.network.model;

import java.util.List;
import lombok.Data;
import org.openlmis.core.model.Pod;

@Data
public class PodsLocalResponse {

  private List<Pod> pods;

}
