FROM biocontainers/biocontainers:v1.1.0_cv2

USER root

ENV FASTQURL=https://www.bioinformatics.babraham.ac.uk/projects/fastqc/fastqc_v0.12.1.zip
ENV DESTZIP=/opt/fastqc/fastqc.zip
ENV DEST=/opt/fastqc

RUN apt-get update && \
  apt-get install -y openjdk-8-jre-headless && \
  mkdir -p $DEST && \
  wget $FASTQURL -O $DESTZIP && \
  unzip $DESTZIP -d $DEST && \
  rm $DESTZIP && \
  mv $DEST/FastQC/* $DEST && \
  rmdir $DEST/FastQC && \
  chmod +x $DEST/fastqc && \
  ln -s $DEST/fastqc /usr/local/bin/fastqc

WORKDIR /data/