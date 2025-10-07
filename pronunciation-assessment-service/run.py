"""Entry point for the pronunciation assessment service."""

import logging
from app.main import app

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

if __name__ == '__main__':
    logger.info("Starting Pronunciation Assessment Microservice (Optimized)...")
    logger.info("Version: 1.0.0-optimized")
    logger.info("Port: 5000")

    # Run the application
    app.run(
        host='0.0.0.0',
        port=5000,
        debug=False,
        threaded=True
    )
