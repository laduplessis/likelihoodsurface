package likelihoodsurface;

import beast.core.Input;
import beast.core.parameter.RealParameter;

/**
 * Created by dlouis on 06/05/16.
 */
public class PointSetter extends RealParameter {

    public final Input<RealParameter> parameterInput = new Input<>("parameter", "Parameter to modify",
            Input.Validate.REQUIRED);



    int index;

    public void initAndValidate() {
        super.initAndValidate();

        index = 0;
    }

    public void setNext() {
        parameterInput.get().setValue(this.getValue(index % this.getDimension()));
        index++;
    }

}
