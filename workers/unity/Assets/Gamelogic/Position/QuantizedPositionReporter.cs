using Improbable.Collections;
using Improbable.Math;
using Improbable.Unity.Visualizer;
using Position;
using UnityEngine;

namespace Assets.Gamelogic.Position
{
    public class QuantizedPositionReporter : MonoBehaviour
    {
        [Require] private QuantizedPosition.Writer Position;

        private QuantizedPosition.Update _positionUpdate = new QuantizedPosition.Update();

        private Option<int> none = new Option<int>(); 

        public void FixedUpdate()
        {
            var position = transform.position;
            var newX = (int) (position.x/Position.Data.scale);
            int newY = (int) (position.y/Position.Data.scale);

            if (newX != Position.Data.x)
            {
                _positionUpdate.x = newX;
            }
            else
            {
                _positionUpdate.x = none;
            }

            if (newY != Position.Data.y)
            {
                _positionUpdate.y = newY;
            }
            else
            {
                _positionUpdate.y = none;
            }

            if (_positionUpdate.x.HasValue || _positionUpdate.y.HasValue)
            {
                Position.Send(_positionUpdate);
            }
        }
    }
}